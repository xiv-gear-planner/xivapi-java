package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.XivApiMapKeys;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiThis;
import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.Map;


@XivApiSheet("ClassJobCategory")
public interface ClassJobCategory extends XivApiObject {
	String getName();

	boolean getNIN();

	boolean getWHM();

	// Should filter out "Name" field
	@XivApiThis
	@XivApiMapKeys("[A-Z]{3}")
	Map<String, Boolean> getJobs();

	// Should include "Name" field
	@XivApiThis
	Map<String, Object> getJobsFull();
}
