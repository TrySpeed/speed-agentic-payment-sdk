package com.tryspeed.server;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class SpeedClient {

	private final String apiKey;
	private final HttpClient httpClient;
	private final ObjectMapper mapper = new ObjectMapper();

	SpeedClient(String apiKey) {
		this.apiKey = apiKey;
		this.httpClient = HttpClient.newHttpClient();
	}

	String createInvoice(long sats) throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("amount", sats);
		body.put("currency", Constants.SPEED_CURRENCY);
		body.put("target_currency", Constants.SPEED_CURRENCY);
		body.put("payment_methods", List.of(Constants.SPEED_PAYMENT_METHOD));

		String auth = "Basic " + Base64.getEncoder().encodeToString((apiKey + ":").getBytes());
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(Constants.SPEED_BASE_URL + "/payments"))
				.header("Authorization", auth)
				.header("Content-Type", "application/json")
				.timeout(Duration.ofSeconds(30))
				.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new RuntimeException(String.format(ErrorMessages.SPEED_API_ERROR, response.statusCode(), response.body()));
		}

		JsonNode json = mapper.readTree(response.body());
		String invoice = json.at("/payment_method_options/lightning/payment_request").asString();
		if (invoice == null || invoice.isBlank()) {
			throw new RuntimeException(ErrorMessages.SPEED_API_MISSING_INVOICE);
		}
		return invoice;
	}
}
