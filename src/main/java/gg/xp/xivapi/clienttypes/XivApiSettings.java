package gg.xp.xivapi.clienttypes;

import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Function;

public final class XivApiSettings {

	private final boolean strict;
	private final URI baseUri;
	private final int concurrencyLimit;
	private final @Nullable String gameVersion;
	private final @Nullable String schemaVersion;

	private XivApiSettings(boolean strict, URI baseUri, int concurrencyLimit, @Nullable String gameVersion, @Nullable String schemaVersion) {
		this.strict = strict;
		this.baseUri = baseUri;
		this.concurrencyLimit = concurrencyLimit;
		this.gameVersion = gameVersion;
		this.schemaVersion = schemaVersion;
	}

	public boolean isStrict() {
		return strict;
	}

	public URI getBaseUri() {
		return baseUri;
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

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {

		boolean strict = true;
		URI baseUri;
		int concurrencyLimit = 10;
		@Nullable String gameVersion;
		@Nullable String schemaVersion;

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

		public Builder setBaseUri(URI baseUri) {
			this.baseUri = baseUri;
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

		public XivApiSettings build() {
			return new XivApiSettings(strict, baseUri, concurrencyLimit, gameVersion, schemaVersion);
		}
//
//		public Builder configure(Function<Builder, Builder> configurer) {
//			return configurer.apply(this);
//		}

		public Builder configure(Consumer<Builder> configurer) {
			configurer.accept(this);
			return this;
		}
	}

}
