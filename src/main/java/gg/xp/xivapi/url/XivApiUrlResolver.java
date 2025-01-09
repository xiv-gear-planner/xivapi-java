package gg.xp.xivapi.url;

import gg.xp.xivapi.assets.AssetFormat;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.util.function.Consumer;

public interface XivApiUrlResolver {
	URIBuilder buildUri();

	URI buildUri(Consumer<URIBuilder> func);

	URI getAssetUri(String assetPath, AssetFormat format);

	URI getAssetUri(String assetPath, String format);
}
