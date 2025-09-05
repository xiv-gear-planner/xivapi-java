package gg.xp.xivapi.clienttypes;

import gg.xp.xivapi.annotations.XivApiMetaField;

public interface XivApiSubrowObject extends XivApiObject {
	@XivApiMetaField("subrow_id")
	int getSubrowId();
}
