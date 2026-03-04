package gg.xp.xivapi.clienttypes;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.function.Consumer;

/**
 * Class which describes xivapi settings.
 * <p>
 * Use {@link #newBuilder()} to construct an instance. See the method javadocs on {@link Builder} for descriptions
 * of what each setting does.
 */
public final class XivApiSettings {

	private final boolean strict;
	private final URI baseUri;
	private final @Nullable URI baseAssetUri;
	private final int concurrencyLimit;
	private final @Nullable String gameVersion;
	private final @Nullable String schemaVersion;
	private final String userAgent;
	private final boolean autoUnwrapValue;
	private final @Nullable HttpClient httpClientOverride;

	private XivApiSettings(boolean strict, URI baseUri, @Nullable URI baseAssetUri, int concurrencyLimit, @Nullable String gameVersion, @Nullable String schemaVersion, String userAgent, boolean autoUnwrapValue, @Nullable HttpClient httpClientOverride) {
		this.strict = strict;
		this.baseUri = baseUri;
		this.baseAssetUri = baseAssetUri;
		this.concurrencyLimit = concurrencyLimit;
		this.gameVersion = gameVersion;
		this.schemaVersion = schemaVersion;
		this.userAgent = userAgent;
		this.autoUnwrapValue = autoUnwrapValue;
		this.httpClientOverride = httpClientOverride;
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

	public boolean isAutoUnwrapValue() {
		return autoUnwrapValue;
	}

	public @Nullable HttpClient getHttpClientOverride() {
		return httpClientOverride;
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
		boolean autoUnwrapValue = true;
		@Nullable HttpClient httpClient;

		{
			try {
				baseUri = new URI("https://v2.xivapi.com/api");
			}
			catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Enable stricter handling of null/undefined/missing/zero values. Defaults to true. Disabling this will cause
		 * null (for objects) or zero/false (for primitives) to be returned instead of throwing an exception in most
		 * circumstances.
		 *
		 * @param strict Whether to enable strict mode.
		 * @return The builder
		 */
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

		/**
		 * Set a concurrency limit for API requests. Defaults to 10. Modify responsibly. Don't tip over Xivapi, but if
		 * you're using your own Boilmaster instance, go wild.
		 *
		 * @param concurrencyLimit Concurrency limit override.
		 * @return The builder
		 */
		public Builder setConcurrencyLimit(int concurrencyLimit) {
			// TODO: this should let you pass in a semaphore directly, so that you can have a concurrency limiter across
			// multiple client instances.
			this.concurrencyLimit = concurrencyLimit;
			return this;
		}

		/**
		 * Set a specific game version to use.
		 *
		 * @param gameVersion The game version to use for API requests.
		 * @return The builder
		 */
		public Builder setGameVersion(@Nullable String gameVersion) {
			this.gameVersion = gameVersion;
			return this;
		}

		/**
		 * Set a specific game version to use.
		 *
		 * @param schemaVersion The schema version to use for API requests.
		 * @return The builder
		 */
		public Builder setSchemaVersion(@Nullable String schemaVersion) {
			this.schemaVersion = schemaVersion;
			return this;
		}

		/**
		 * Override the default user agent.
		 *
		 * @param userAgent The user agent string to use.
		 * @return The builder.
		 */
		public Builder setUserAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		/**
		 * Set whether the client will detect and unwrap values of the form {@code {"value":actualValue}}. Defaults to
		 * true. If set to false, then those will instead throw an exception, and you will need to instead use a
		 * XivApiStruct type to deserialize that value.
		 *
		 * @param autoUnwrapValue Whether to automatically unwrap wrapped values.
		 * @return The builder.
		 */
		public Builder setAutoUnwrapValue(boolean autoUnwrapValue) {
			this.autoUnwrapValue = autoUnwrapValue;
			return this;
		}

		/**
		 * Set a custom HttpClient to use for API requests.
		 *
		 * @param httpClient The HttpClient to use.
		 * @return The builder.
		 */
		public Builder setHttpClient(@Nullable HttpClient httpClient) {
			this.httpClient = httpClient;
			return this;
		}

		/**
		 * Method that allows you to supply configurations to the builder in a re-usable manner, without breaking
		 * the builder pattern by passing the builder into another method.
		 *
		 * @param configurer A function that takes the builder as an argument. It should modify the builder in-place.
		 * @return The builder.
		 */
		public Builder configure(Consumer<Builder> configurer) {
			configurer.accept(this);
			return this;
		}

		public XivApiSettings build() {
			return new XivApiSettings(strict, baseUri, baseAssetUri, concurrencyLimit, gameVersion, schemaVersion, userAgent, autoUnwrapValue, httpClient);
		}
	}

}
