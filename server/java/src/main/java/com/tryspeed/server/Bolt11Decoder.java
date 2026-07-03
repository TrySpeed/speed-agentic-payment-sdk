package com.tryspeed.server;

import java.util.Locale;

public class Bolt11Decoder {

	public static String getPaymentHash(String invoice) {
		String lower = invoice.toLowerCase(Locale.ROOT);
		int sep = lower.lastIndexOf('1');
		if (sep < 0 || lower.length() - 6 <= sep) {
			throw new Exceptions.InvalidInvoiceException(ErrorMessages.INVALID_BOLT11);
		}

		// Strip the 6-char bech32 checksum from the end
		String dataPart = lower.substring(sep + 1, lower.length() - 6);
		byte[] words = toFiveBitWords(dataPart);

		int pos = Constants.BOLT11_TIMESTAMP_WORDS;
		while (pos + 3 <= words.length) {
			int type = words[pos];
			int len = words[pos + 1] * 32 + words[pos + 2];
			pos += 3;
			if (type == Constants.BOLT11_PAYMENT_HASH_TYPE && len == Constants.BOLT11_PAYMENT_HASH_LENGTH) {
				return convertBits(words, pos, len);
			}
			pos += len;
		}
		throw new Exceptions.InvalidInvoiceException(ErrorMessages.PAYMENT_HASH_NOT_FOUND);
	}

	private static byte[] toFiveBitWords(String data) {
		byte[] words = new byte[data.length()];
		for (int i = 0; i < data.length(); i++) {
			byte val = (byte) Constants.BECH32_CHARSET.indexOf(data.charAt(i));
			if (val < 0) {
				throw new Exceptions.InvalidInvoiceException(ErrorMessages.INVALID_BECH32_CHAR + data.charAt(i));
			}
			words[i] = val;
		}
		return words;
	}

	// Convert 5-bit words to a hex string (big-endian, discarding padding bits)
	private static String convertBits(byte[] data, int start, int len) {
		int acc = 0, bits = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < start + len; i++) {
			acc = (acc << 5) | data[i];
			bits += 5;
			while (bits >= 8) {
				bits -= 8;
				sb.append(String.format("%02x", (acc >> bits) & 0xFF));
			}
		}
		return sb.toString();
	}
}
