package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Map;

public interface XivApiLangValue<X> extends Serializable {

	@JsonIgnore // ignore because the individual fields are already serialized
	Map<String, X> getAll();

	default X get(String lang) {
		return getAll().get(lang);
	}

	default X getEn() {
		return get("en");
	}

	default X getDe() {
		return get("de");
	}

	default X getFr() {
		return get("fr");
	}

	default X getJa() {
		return get("ja");
	}

	@JsonIgnore
	@Deprecated
	default X getJp() {
		return get("ja");
	}
}
