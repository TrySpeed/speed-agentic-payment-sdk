package com.tryspeed.server;

import java.util.List;

public final class L402Config {

	private final String speedApiKey;
	private final String macaroonSecret;
	private final List<RouteConfig> routes;

	private L402Config(Builder builder) {
		this.speedApiKey = builder.speedApiKey;
		this.macaroonSecret = builder.macaroonSecret;
		this.routes = List.copyOf(builder.routes);
	}

	public String speedApiKey() {return speedApiKey;}

	public String macaroonSecret() {return macaroonSecret;}

	public List<RouteConfig> routes() {return routes;}

	public static Builder builder() {return new Builder();}

	public static final class Builder {

		private String speedApiKey;
		private String macaroonSecret;
		private List<RouteConfig> routes = List.of();

		public Builder speedApiKey(String speedApiKey) {
			this.speedApiKey = speedApiKey;
			return this;
		}

		public Builder macaroonSecret(String macaroonSecret) {
			this.macaroonSecret = macaroonSecret;
			return this;
		}

		public Builder routes(List<RouteConfig> routes) {
			this.routes = routes;
			return this;
		}

		public Builder routes(RouteConfig... routes) {
			this.routes = List.of(routes);
			return this;
		}

		public L402Config build() {
			if (speedApiKey == null) {
				throw new IllegalStateException(ErrorMessages.SPEED_API_KEY_REQUIRED);
			}
			if (macaroonSecret == null) {
				throw new IllegalStateException(ErrorMessages.MACAROON_SECRET_REQUIRED);
			}
			return new L402Config(this);
		}
	}
}
