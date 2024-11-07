package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public interface XivApiLangString {

	default String get(String lang) {
		return getAll().get(lang);
	};

	@JsonIgnore // ignore because the individual fields are already serialized
	Map<String, String> getAll();

	default String getEn() {
		return get("en");
	}

	default String getDe() {
		return get("de");
	}

	default String getFr() {
		return get("fr");
	}

	default String getJp() {
		return get("jp");
	}
}
