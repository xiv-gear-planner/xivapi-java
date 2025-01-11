package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

public interface XivApiBase extends Serializable {
	// Mainly useful for debugging - generally should not be used in production code
	@JsonIgnore
	Map<Method, Object> getMethodValueMap();
	XivApiSchemaVersion getSchemaVersion();
}
