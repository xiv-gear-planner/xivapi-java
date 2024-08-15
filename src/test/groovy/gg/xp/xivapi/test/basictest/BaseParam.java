package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.List;

public interface BaseParam extends XivApiObject {

	Integer[] getMeldParam();

	@XivApiField("MeldParam")
	List<Integer> getMeldParams();

}
