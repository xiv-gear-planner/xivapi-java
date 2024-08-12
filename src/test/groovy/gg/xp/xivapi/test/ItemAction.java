package gg.xp.xivapi.test;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiObject;

public interface ItemAction extends XivApiObject {
	boolean getCondBattle();

	@XivApiField("Type")
	int type();

}
