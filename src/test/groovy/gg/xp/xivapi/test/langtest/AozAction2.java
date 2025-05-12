package gg.xp.xivapi.test.langtest;

import gg.xp.xivapi.annotations.XivApiAs;
import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiLangValue;
import gg.xp.xivapi.clienttypes.XivApiObject;

@XivApiSheet("AozAction")
public interface AozAction2 extends XivApiObject {

	// You can also map a sub-struct to all langs
	@XivApiTransientField("Location")
	XivApiLangValue<Location> getLocationsAll();

	@XivApiTransientField
	@XivApiAs("html")
	XivApiLangValue<String> getDescription();
}
