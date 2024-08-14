package gg.xp.xivapi.test;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.List;

public interface ClassJob extends XivApiObject {

	Quest getUnlockQuest();

//	int[] getDataHQ();
//
//	@XivApiField("DataHQ")
//	Integer[] getDataHQ2();

}
