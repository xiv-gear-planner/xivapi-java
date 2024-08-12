package gg.xp.xivapi.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RawFieldMapper<X> extends NormalFieldMapper<X> {

	public RawFieldMapper(String fieldName, Class<X> fieldType, ObjectMapper mapper) {
		super(fieldName + "@as(raw)", fieldType, mapper);
	}

}
