package gg.xp.xivapi.mappers.objects;

import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.RootQueryFieldsBuilder;
import org.apache.hc.core5.http.NameValuePair;

import java.util.Collections;
import java.util.List;

public class RootMapper<X> {

	private final FieldMapper<X> wrapped;
	private final List<NameValuePair> queryFields;

	public RootMapper(FieldMapper<X> wrapped) {
		this.wrapped = wrapped;
		var rootQfb = new RootQueryFieldsBuilder();
		wrapped.buildQueryFields(rootQfb);
		queryFields = rootQfb.formatQueryFields();
	}

	public FieldMapper<X> getWrappedMapper() {
		return wrapped;
	}

	public List<NameValuePair> getQueryFields() {
		return Collections.unmodifiableList(queryFields);
	}
}
