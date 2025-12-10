package gg.xp.xivapi.clienttypes;

import gg.xp.xivapi.assets.AssetFormat;

import java.io.Serializable;
import java.net.URI;

public interface XivApiAsset<X extends AssetFormat> extends Serializable {
	URI getURI(String formatRaw);
	URI getURI(X format);
}
