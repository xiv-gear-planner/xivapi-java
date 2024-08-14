package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.List;

@NullIfZero
public interface ItemAction extends XivApiObject {
	boolean getCondBattle();

	@XivApiField("Type")
	int type();

	List<Integer> getData();

//	int[] getDataHQ();
//
//	@XivApiField("DataHQ")
//	Integer[] getDataHQ2();

}
