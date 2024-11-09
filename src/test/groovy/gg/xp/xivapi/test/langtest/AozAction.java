package gg.xp.xivapi.test.langtest;

import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiLangValue;
import gg.xp.xivapi.clienttypes.XivApiObject;

@XivApiSheet
public interface AozAction extends XivApiObject {

	@XivApiRaw
	@XivApiTransientField
	int getLocation();

	// This ensures that we can properly load sub-fields in a transient
	// This contains translated strings inside of it
	@XivApiTransientField("Location")
	LocationIntl getLocationFull();

	// You can select a language to be used for non-language-mapped fields on a sub-object
	@XivApiTransientField("Location")
	@XivApiLang("de")
	Location getLocationDe();

	// You can also map a sub-struct to all langs
	@XivApiTransientField("Location")
	XivApiLangValue<Location> getLocationsAll();
}
