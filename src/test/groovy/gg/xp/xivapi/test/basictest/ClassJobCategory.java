package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.clienttypes.XivApiObject;

@XivApiSheet("ClassJobCategory")
public interface ClassJobCategory extends XivApiObject {
	String getName();

	boolean getNIN();

	boolean getWHM();
}
