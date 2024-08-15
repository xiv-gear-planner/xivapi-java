package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

public class RawTransientFieldMapper<X> extends TransientFieldMapper<X> {

	public RawTransientFieldMapper(String fieldName, Class<X> fieldType, Method method, ObjectMapper mapper) {
		super(fieldName + "@as(raw)", fieldType, method, mapper);
	}

}
