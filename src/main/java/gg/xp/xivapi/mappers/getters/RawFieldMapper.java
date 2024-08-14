package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

public class RawFieldMapper<X> extends NormalFieldMapper<X> {

	public RawFieldMapper(String fieldName, Class<X> fieldType, Method method, ObjectMapper mapper) {
		super(fieldName + "@as(raw)", fieldType, method, mapper);
	}

}
