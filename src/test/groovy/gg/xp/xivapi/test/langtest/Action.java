package gg.xp.xivapi.test.langtest;

import gg.xp.xivapi.annotations.XivApiAs;
import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiLangValue;
import gg.xp.xivapi.clienttypes.XivApiObject;

@XivApiSheet
public interface Action extends XivApiObject {

	// This field is purely to make the API not query all the fields
	// TODO: make that the default behavior
	String getName();

	@XivApiTransientField("Description")
	String getDescriptionDefault();

	@XivApiTransientField("Description")
	@XivApiAs("html")
	String getDescriptionHtml();

	@XivApiTransientField("Description")
	@XivApiRaw
	String getDescriptionRaw();

	@XivApiTransientField("Description")
	@XivApiLang("de")
	String getDescriptionDefaultDe();

	@XivApiTransientField("Description")
	@XivApiAs("html")
	@XivApiLang("de")
	String getDescriptionHtmlDe();

	@XivApiTransientField("Description")
	@XivApiRaw
	@XivApiLang("de")
	String getDescriptionRawDe();

	@XivApiTransientField("Description")
	XivApiLangValue<String> getDescriptionAll();

	@XivApiTransientField("Description")
	@XivApiAs("html")
	XivApiLangValue<String> getDescriptionHtmlAll();

	@XivApiTransientField("Description")
	@XivApiRaw
	XivApiLangValue<String> getDescriptionRawAll();
}
