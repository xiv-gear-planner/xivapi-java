package gg.xp.xivapi.assets;

import java.util.Locale;

public enum ImageFormat implements AssetFormat {

	JPEG,
	PNG,
	WEBP;

	public String getAssetFormatParam() {
		return name().toLowerCase(Locale.ROOT);
	}
}
