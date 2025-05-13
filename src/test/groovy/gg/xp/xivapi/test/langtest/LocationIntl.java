package gg.xp.xivapi.test.langtest;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.clienttypes.XivApiLangValue;
import gg.xp.xivapi.clienttypes.XivApiObject;

public interface LocationIntl extends XivApiObject {
	// Default language
	String getName();

	// Explicitly request DE
	@XivApiField("Name")
	@XivApiLang("de")
	@XivApiRaw
	String getNameDe();

	// Request all known languages
	@XivApiField("Name")
	XivApiLangValue<String> getNameStrings();
}
