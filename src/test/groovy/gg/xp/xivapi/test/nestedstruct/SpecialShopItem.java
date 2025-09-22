package gg.xp.xivapi.test.nestedstruct;

import gg.xp.xivapi.clienttypes.XivApiStruct;

import java.util.List;

public interface SpecialShopItem extends XivApiStruct {
	List<Item> getItem();
	List<Integer> getReceiveCount();
	AchivementUnlock getAchievementUnlock();
}
