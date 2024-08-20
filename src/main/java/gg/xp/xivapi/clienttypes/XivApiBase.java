package gg.xp.xivapi.clienttypes;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

public interface XivApiBase extends Serializable {
	Map<Method, Object> getMethodValueMap();
	XivApiSchemaVersion getSchemaVersion();
}
