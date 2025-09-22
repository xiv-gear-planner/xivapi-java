package gg.xp.xivapi.test.nestedstruct;

import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.List;

public interface SpecialShop extends XivApiObject {

	List<SpecialShopItem> getItem();

}
