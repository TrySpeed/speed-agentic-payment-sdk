package com.tryspeed.server;

import com.github.nitram509.jmacaroons.Macaroon;
import com.github.nitram509.jmacaroons.MacaroonsVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

class MacaroonHelper {

	static String create(RouteConfig config, String bolt11Invoice, String macaroonSecret) {

		String paymentHash = Bolt11Decoder.getPaymentHash(bolt11Invoice);
		String id = UUID.randomUUID().toString();
		Macaroon macaroon = Macaroon.builder("", macaroonSecret, id)
				.addCaveat(Constants.CAVEAT_METHOD + " = " + config.method())
				.addCaveat(Constants.CAVEAT_PATH + " = " + config.path())
				.addCaveat(Constants.CAVEAT_SATS + " = " + config.sats())
				.addCaveat(Constants.CAVEAT_PAYMENT_HASH + " = " + paymentHash)
				.build();
		return macaroon.serialize();
	}

	static void verify(String encodedMacaroon, RouteConfig config, String macaroonSecret, String paymentHash) {

		Macaroon macaroon = Macaroon.deserialize(encodedMacaroon);
		MacaroonsVerifier verifier = new MacaroonsVerifier(macaroon);
		verifier.satisfyExact(Constants.CAVEAT_METHOD + " = " + config.method());
		verifier.satisfyExact(Constants.CAVEAT_PATH + " = " + config.path());
		verifier.satisfyExact(Constants.CAVEAT_SATS + " = " + config.sats());
		verifier.satisfyExact(Constants.CAVEAT_PAYMENT_HASH + " = " + paymentHash);
		if (!verifier.isValid(macaroonSecret)) {
			throw new Exceptions.InvalidMacaroonException(ErrorMessages.MACAROON_SIGNATURE_INVALID);
		}
	}

	static String getPaymentHash(String encodedMacaroon) {

		Macaroon macaroon = Macaroon.deserialize(encodedMacaroon);
		Optional<String> paymentHashOpt = Arrays.stream(macaroon.caveatPackets)
				.map(caveatPacket -> new String(caveatPacket.getRawValue(), StandardCharsets.UTF_8))
				.filter(caveat -> caveat.startsWith(Constants.CAVEAT_PAYMENT_HASH + " = "))
				.findFirst();

		String prefix = Constants.CAVEAT_PAYMENT_HASH + " = ";
		return paymentHashOpt.map(caveat -> caveat.substring(prefix.length()))
				.orElseThrow(() -> new Exceptions.InvalidMacaroonException(ErrorMessages.MISSING_PAYMENT_HASH_CAVEAT));
	}

	static byte[] fromHex(String hex) {
		int len = hex.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		}
		return data;
	}

	static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
