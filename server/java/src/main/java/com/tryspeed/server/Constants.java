package com.tryspeed.server;

final class Constants {

	private Constants() {}

	static final String BECH32_CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
	static final String CAVEAT_METHOD = "method";
	static final String CAVEAT_PATH = "path";
	static final String CAVEAT_PAYMENT_HASH = "payment_hash";
	static final String CAVEAT_SATS = "sats";
	static final String L402_SCHEME = "L402";
	static final String SPEED_BASE_URL = "https://api.tryspeed.com";
	static final String SPEED_CURRENCY = "SATS";
	static final String SPEED_PAYMENT_METHOD = "lightning";
	static final int BOLT11_PAYMENT_HASH_LENGTH = 52;
	static final int BOLT11_PAYMENT_HASH_TYPE = 1;
	static final int BOLT11_TIMESTAMP_WORDS = 7;
}
