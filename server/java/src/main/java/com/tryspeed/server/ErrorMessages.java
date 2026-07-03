package com.tryspeed.server;

final class ErrorMessages {

	private ErrorMessages() {}

	static final String INTERNAL_SERVER_ERROR = "Internal server error";
	static final String INVALID_BECH32_CHAR = "Invalid bech32 character: ";
	static final String INVALID_BOLT11 = "Not a valid BOLT11 invoice";
	static final String INVALID_PAYMENT_PREIMAGE = "Invalid payment preimage";
	static final String MACAROON_SECRET_REQUIRED = "macaroonSecret is required";
	static final String MACAROON_SIGNATURE_INVALID = "Macaroon signature is invalid";
	static final String MALFORMED_L402_CREDENTIALS = "Malformed L402 credentials";
	static final String MISSING_PAYMENT_HASH_CAVEAT = "Missing payment_hash caveat in macaroon";
	static final String PAYMENT_ALREADY_PROCESSING = "Payment is already being processed";
	static final String PAYMENT_HASH_NOT_FOUND = "payment_hash not found in BOLT11 invoice";
	static final String SPEED_API_ERROR = "Speed API error (%d): %s";
	static final String SPEED_API_KEY_REQUIRED = "speedApiKey is required";
	static final String SPEED_API_MISSING_INVOICE = "Speed API response missing lightning payment_request";
}
