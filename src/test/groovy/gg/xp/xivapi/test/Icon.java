package gg.xp.xivapi.test;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiSubStruct;

import java.net.URI;
import java.net.URISyntaxException;

public interface Icon extends XivApiSubStruct {
	@XivApiField("id")
	int getId();

	@XivApiField("path")
	String getPath();

	@XivApiField("path_hr1")
	String getPathHD();

	// TODO: this should move, otherwise it won't be possible to change the base url
	default URI getPngIconUrl() {
		try {
			return new URI("https://beta.xivapi.com/api/1/asset/" + getPath() + "?format=png");
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
