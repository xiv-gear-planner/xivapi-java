package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.clienttypes.XivApiObject;

public interface Quest extends XivApiObject {

	@XivApiRaw
	int getExpFactor();

	PlaceName getPlaceName();

}
