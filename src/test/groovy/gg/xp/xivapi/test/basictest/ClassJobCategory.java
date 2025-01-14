package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.XivApiMapKeys;
import gg.xp.xivapi.annotations.XivApiSheet;
import gg.xp.xivapi.annotations.XivApiThis;
import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.Map;


/*
TODO: this ends up taking an unexpectedly large amount of memory.
This is a good use case for some kind of re-usable element caching.
For example, if two items have the same primary key on their ClassJobCategory,
and the two values are equal (including schema version and all), then they
should be replaced with the same object internally.
We would need to ensure that there is nothing that would affect the representation
of the object that lives out of the object itself. I don't think there is, given that
ObjectFieldMapper only takes the target class and ObjectMapper as its arguments.
*/
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
