package com.tryspeed.server;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class L402Filter implements Filter {

	private final Map<String, RouteConfig> endpointConfigMap = new ConcurrentHashMap<>();
	private final String macaroonSecret;
	private final SpeedClient speedClient;
	private final Map<String, Boolean> lock = new ConcurrentHashMap<>();
	private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();

	public L402Filter(L402Config config) {
		this.macaroonSecret = config.macaroonSecret();
		this.speedClient = new SpeedClient(config.speedApiKey());
		for (RouteConfig route : config.routes()) {
			endpointConfigMap.put(route.method() + " " + route.path(), route);
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		RouteConfig config = endpointConfigMap.get(request.getMethod() + " " + getPath(request));

		if (config == null || config.sats() == 0) {
			chain.doFilter(req, res);
			return;
		}

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || authHeader.isBlank()) {
			handleMissingPayment(config, response);
			return;
		}

		handlePaymentPresent(request, response, chain, authHeader.trim(), config);
	}

	private void handleMissingPayment(RouteConfig config, HttpServletResponse response) throws IOException {
		try {
			String lightningInvoice = speedClient.createInvoice(config.sats());
			String macaroon = MacaroonHelper.create(config, lightningInvoice, macaroonSecret);
			response.setStatus(402);
			response.setContentType("application/json");
			response.setHeader("WWW-Authenticate", Constants.L402_SCHEME + " macaroon=\"" + macaroon + "\", invoice=\"" + lightningInvoice + "\"");
			response.getWriter().write("{}");
		} catch (Exception e) {
			writeError(response, 500, ErrorMessages.INTERNAL_SERVER_ERROR);
		}
	}

	private void handlePaymentPresent(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String authHeader,
			RouteConfig config) throws IOException, ServletException {

		String credentials = authHeader.replaceFirst("(?i)" + Constants.L402_SCHEME + "\\s+", "");
		String[] parts = credentials.split(":");
		if (parts.length != 2) {
			writeError(response, 400, ErrorMessages.MALFORMED_L402_CREDENTIALS);
			return;
		}

		String encodedMacaroon = parts[0].trim();
		String receivedPreimage = parts[1].trim();

		if (encodedMacaroon.isBlank() || receivedPreimage.isBlank()) {
			writeError(response, 400, ErrorMessages.MALFORMED_L402_CREDENTIALS);
			return;
		}

		String paymentHash;
		try {
			paymentHash = MacaroonHelper.getPaymentHash(encodedMacaroon);
			MacaroonHelper.verify(encodedMacaroon, config, macaroonSecret, paymentHash);
		} catch (Exception e) {
			writeError(response, 400, ErrorMessages.MALFORMED_L402_CREDENTIALS);
			return;
		}
		if (!isPreimageValid(paymentHash, receivedPreimage)) {
			writeError(response, 401, ErrorMessages.INVALID_PAYMENT_PREIMAGE);
			return;
		}

		CachedResponse cached = cache.get(paymentHash);
		if (cached != null) {
			writeCachedResponse(response, cached);
			return;
		}

		if (lock.putIfAbsent(paymentHash, Boolean.TRUE) != null) {
			writeError(response, 409, ErrorMessages.PAYMENT_ALREADY_PROCESSING);
			return;
		}

		try {
			CapturingResponseWrapper capturing = new CapturingResponseWrapper(response);
			chain.doFilter(request, capturing);

			byte[] body = capturing.getContent();
			cache.put(paymentHash, new CachedResponse(capturing.getCapturedStatus(), capturing.getCapturedContentType(), body));
		} finally {
			lock.remove(paymentHash);
		}
	}

	private boolean isPreimageValid(String paymentHash, String receivedPreimage) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(MacaroonHelper.fromHex(receivedPreimage));
			return paymentHash.equals(MacaroonHelper.toHex(hash));
		} catch (Exception e) {
			return false;
		}
	}

	private void writeCachedResponse(HttpServletResponse response, CachedResponse cached) throws IOException {
		response.setStatus(cached.status());
		if (cached.contentType() != null) {
			response.setContentType(cached.contentType());
		}
		response.getOutputStream().write(cached.body());
	}

	private void writeError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.getWriter().write("{\"message\":\"" + message + "\"}");
	}

	private static String getPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		return (ctx != null && !ctx.isEmpty()) ? uri.substring(ctx.length()) : uri;
	}
}
