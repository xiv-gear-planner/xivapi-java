package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

public interface XivApiBase extends Serializable {
	@JsonIgnore
	Map<Method, Object> getMethodValueMap();
	XivApiSchemaVersion getSchemaVersion();
}
