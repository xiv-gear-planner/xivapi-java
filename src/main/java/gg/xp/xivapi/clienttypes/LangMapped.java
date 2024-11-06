package gg.xp.xivapi.clienttypes;

import java.util.Map;

public interface LangMapped<X> {

	X get(String lang);

	Map<String, X> asMap();

	// Groovy convenience method
	default X getAt(String lang) {
		return get(lang);
	}
}
