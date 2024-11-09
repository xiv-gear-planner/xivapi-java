package gg.xp.xivapi.test.pagertest;

import gg.xp.xivapi.annotations.XivApiLang;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiTransientField;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.test.basictest.Icon;

@XivApiSheet
public interface AozAction extends XivApiObject {

	Action getAction();

	byte getRank();

	@XivApiTransientField
	Icon getIcon();

	@XivApiTransientField("Description")
	String description();

	@XivApiRaw
	@XivApiTransientField
	int getLocation();

}
