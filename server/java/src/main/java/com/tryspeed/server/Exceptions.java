package com.tryspeed.server;

public class Exceptions {

	public static class L402Exception extends RuntimeException {

		public L402Exception(String message) {super(message);}
	}

	public static class InvalidMacaroonException extends L402Exception {

		public InvalidMacaroonException(String message) {super(message);}
	}

	public static class InvalidInvoiceException extends L402Exception {

		public InvalidInvoiceException(String message) {super(message);}
	}
}
