package gg.xp.xivapi.test.langtest;

import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.test.basictest.Icon;

@XivApiSheet
public interface AozAction extends XivApiObject {

	@XivApiRaw
	@XivApiTransientField
	int getLocation();

	// This ensures that we can properly load sub-fields in a transient
	// This contains translated strings inside of it
	@XivApiTransientField("Location")
	LocationDe getLocationFull();

	// TODO: XivApi does not seem to support this fully. It works if you do an "everything" query, i.e. query
	// for Location@lang(de), but not if you try to get a sub-field out of it, i.e. Location@lang(de).name.
	// You have to do Location.name@lang(de).
	// Also check that the language annotation works at higher levels
	@XivApiTransientField("Location")
	@XivApiLang("de")
	Location getLocationDe();

}
