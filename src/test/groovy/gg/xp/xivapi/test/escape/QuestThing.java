package gg.xp.xivapi.test.escape;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.clienttypes.XivApiObject;

@XivApiSheet("quest/054/KinGmj101_05426")
public interface QuestThing extends XivApiObject {
	@XivApiField("unknown0")
	String getUnknown0();
	@XivApiField("unknown4")
	String getUnknown4();
}
