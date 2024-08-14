package gg.xp.xivapi.test.pagertest;

import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.clienttypes.XivApiObject;

@XivApiSheet
public interface AozAction extends XivApiObject {

	Action getAction();

	byte getRank();

}
