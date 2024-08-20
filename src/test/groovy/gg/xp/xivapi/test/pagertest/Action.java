package gg.xp.xivapi.test.pagertest;

import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.clienttypes.XivApiObject;

@XivApiSheet("Action")
public interface Action extends XivApiObject {

	String getName();

}
