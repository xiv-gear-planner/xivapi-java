package gg.xp.xivapi.test.langtest;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.clienttypes.XivApiObject;

public interface LocationDe extends XivApiObject {
	String getName();

	@XivApiField("Name")
	@XivApiLang("de")
	String getNameDe();
}
