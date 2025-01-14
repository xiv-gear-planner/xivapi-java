package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.XivApiAssetPath;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.assets.ImageFormat;
import gg.xp.xivapi.clienttypes.XivApiAsset;
import gg.xp.xivapi.clienttypes.XivApiStruct;

import java.net.URI;
import java.net.URISyntaxException;

public interface Icon extends XivApiStruct {
	@XivApiField("id")
	int getId();

	@XivApiField("path")
	String getPath();

	@XivApiField("path_hr1")
	String getPathHD();

	@XivApiField("path_hr1")
	XivApiAsset<ImageFormat> getAssetPathHD();

	@XivApiField("path_hr1")
	@XivApiAssetPath(format = "jpg")
	URI getAssetPathHDasURI();

	default URI getPngIconUrl() {
		try {
			return new URI("https://beta.xivapi.com/api/1/asset/" + getPath() + "?format=png");
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
