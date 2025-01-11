package gg.xp.xivapi.clienttypes;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public final class XivApiSettings {

	private final boolean strict;
	private final URI baseUri;
	private final @Nullable URI baseAssetUri;
	private final int concurrencyLimit;
	private final @Nullable String gameVersion;
	private final @Nullable String schemaVersion;
	private final String userAgent;

	private XivApiSettings(boolean strict, URI baseUri, @Nullable URI baseAssetUri, int concurrencyLimit, @Nullable String gameVersion, @Nullable String schemaVersion, String userAgent) {
		this.strict = strict;
		this.baseUri = baseUri;
		this.baseAssetUri = baseAssetUri;
		this.concurrencyLimit = concurrencyLimit;
		this.gameVersion = gameVersion;
		this.schemaVersion = schemaVersion;
		this.userAgent = userAgent;
	}

	public boolean isStrict() {
		return strict;
	}

	public URI getBaseUri() {
		return baseUri;
	}

	public URI getBaseAssetUri() {
		return baseAssetUri == null ? baseUri : baseAssetUri;
	}

	public int getConcurrencyLimit() {
		return concurrencyLimit;
	}

	public @Nullable String getGameVersion() {
		return gameVersion;
	}

	public @Nullable String getSchemaVersion() {
		return schemaVersion;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {

		boolean strict = true;
		URI baseUri;
		@Nullable URI baseAssetUri;
		int concurrencyLimit = 10;
		@Nullable String gameVersion;
		@Nullable String schemaVersion;
		String userAgent = "Xivapi-Java";

		{
			try {
				baseUri = new URI("https://beta.xivapi.com/api/1");
			}
			catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}

		public Builder setStrict(boolean strict) {
			this.strict = strict;
			return this;
		}

		/**
		 * Set the base URL. Should include the "/api/1" part of the URL.
		 *
		 * @param baseUri The new base URL
		 * @return The builder
		 */
		public Builder setBaseUri(URI baseUri) {
			this.baseUri = baseUri;
			return this;
		}

		/**
		 * Set an alternative base URL for assets. Should include the "/api/1" part of the URL.
		 *
		 * @param baseAssetUri The new base URL
		 * @return The builder
		 */
		public Builder setBaseAssetUri(@Nullable URI baseAssetUri) {
			this.baseAssetUri = baseAssetUri;
			return this;
		}

		public Builder setConcurrencyLimit(int concurrencyLimit) {
			this.concurrencyLimit = concurrencyLimit;
			return this;
		}

		public Builder setGameVersion(@Nullable String gameVersion) {
			this.gameVersion = gameVersion;
			return this;
		}

		public Builder setSchemaVersion(@Nullable String schemaVersion) {
			this.schemaVersion = schemaVersion;
			return this;
		}
//
//		public Builder configure(Function<Builder, Builder> configurer) {
//			return configurer.apply(this);
//		}

		public Builder setUserAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		public Builder configure(Consumer<Builder> configurer) {
			configurer.accept(this);
			return this;
		}

		public XivApiSettings build() {
			return new XivApiSettings(strict, baseUri, baseAssetUri, concurrencyLimit, gameVersion, schemaVersion, userAgent);
		}
	}

}
