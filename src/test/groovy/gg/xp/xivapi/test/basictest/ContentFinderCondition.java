package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.EmptyStringNull;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiObject;

public interface ContentFinderCondition extends XivApiObject {
	@EmptyStringNull
	@XivApiField("Name")
	String getName();
	@XivApiField("Name")
	String getNameNotNull();
}
