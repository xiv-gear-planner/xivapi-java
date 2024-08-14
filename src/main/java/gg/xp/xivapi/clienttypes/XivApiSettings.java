package gg.xp.xivapi.clienttypes;

import java.net.URI;
import java.net.URISyntaxException;

public final class XivApiSettings {

	private final boolean strict;
	private final URI baseUri;
	private final int concurrencyLimit;

	private XivApiSettings(boolean strict, URI baseUri, int concurrencyLimit) {
		this.strict = strict;
		this.baseUri = baseUri;
		this.concurrencyLimit = concurrencyLimit;
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

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {

		boolean strict = true;
		URI baseUri;
		int concurrencyLimit = 10;

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

		public XivApiSettings build() {
			return new XivApiSettings(strict, baseUri, concurrencyLimit);
		}

	}

}
