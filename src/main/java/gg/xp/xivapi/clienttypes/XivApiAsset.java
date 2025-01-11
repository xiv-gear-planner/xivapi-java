package gg.xp.xivapi.clienttypes;

import gg.xp.xivapi.assets.AssetFormat;

import java.net.URI;

public interface XivApiAsset<X extends AssetFormat> {
	URI getURI(String formatRaw);
	URI getURI(X format);
}
