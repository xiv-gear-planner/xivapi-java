package gg.xp.xivapi.impl;

import gg.xp.xivapi.assets.AssetFormat;
import gg.xp.xivapi.clienttypes.XivApiSettings;
import gg.xp.xivapi.exceptions.XivApiException;
import gg.xp.xivapi.url.XivApiUrlResolver;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UrlResolverImpl implements XivApiUrlResolver, Serializable {
	@Serial
	private static final long serialVersionUID = -1458487307014327401L;
	private final URI baseUri;
	private final URI baseAssetUri;
	private final List<NameValuePair> defaultParams;

	public UrlResolverImpl(XivApiSettings settings) {
		this.baseUri = settings.getBaseUri();
		this.baseAssetUri = settings.getBaseAssetUri();
		this.defaultParams = makeDefaultParams(settings);
	}

	private static List<NameValuePair> makeDefaultParams(XivApiSettings settings) {
		List<NameValuePair> out = new ArrayList<>();
		if (settings.getGameVersion() != null) {
			out.add(new BasicNameValuePair("version", settings.getGameVersion()));
		}
		if (settings.getSchemaVersion() != null) {
			out.add(new BasicNameValuePair("schema", settings.getSchemaVersion()));
		}
		return out;
	}

	/**
	 * @return A URI builder, initialized to the base URI plus default query params
	 */
	@Override
	public URIBuilder buildUri() {
		return new URIBuilder(baseUri).addParameters(defaultParams);
	}

	/**
	 * Given a lambda, configure a URIBuilder with that lambda. Starts with {@link #buildUri()}.
	 *
	 * @param func The lambda to use to configure the builder
	 * @return the built URI
	 */
	@Override
	public URI buildUri(Consumer<URIBuilder> func) {
		URIBuilder builder = buildUri();
		func.accept(builder);
		try {
			return builder.build();
		}
		catch (URISyntaxException e) {
			throw new XivApiException("Error constructing URI", e);
		}
	}

	@Override
	public URI getAssetUri(String assetPath, AssetFormat format) {
		return getAssetUri(assetPath, format.getAssetFormatParam());
	}

	@Override
	public URI getAssetUri(String assetPath, String format) {
		try {
			return new URIBuilder(baseAssetUri).appendPath("/asset").appendPath(assetPath).addParameter("format", format).build();
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
