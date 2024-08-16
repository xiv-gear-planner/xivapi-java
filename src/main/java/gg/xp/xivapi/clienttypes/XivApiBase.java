package gg.xp.xivapi.clienttypes;

import java.lang.reflect.Method;
import java.util.Map;

public interface XivApiBase {
	Map<Method, Object> getMethodValueMap();
	XivApiSchemaVersion getSchemaVersion();
}
