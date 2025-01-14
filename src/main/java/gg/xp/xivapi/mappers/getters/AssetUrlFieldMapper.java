package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.XivApiAssetPath;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

public class AssetUrlFieldMapper<X> implements FieldMapper<X> {

	private final Class<X> returnType;
	private final ObjectMapper mapper;
	private final String format;

	public AssetUrlFieldMapper(Class<X> returnType, Method method, ObjectMapper mapper) {
		this.returnType = returnType;
		this.mapper = mapper;
		XivApiAssetPath ann = method.getAnnotation(XivApiAssetPath.class);
		if (ann == null) {
			throw new IllegalArgumentException("@XivApiAssetPath annotation is required");
		}
		format = ann.format();
	}

	@Override
	public X getValue(JsonNode current, XivApiContext context) {
		try {
			String raw = current.textValue();
			URI out = context.urlResolver().getAssetUri(raw, format);
			// Possible future improvement - intern pieces of the URI
//			if (returnType.equals(URI.class)) {
//				String scheme = out.getScheme();
//				String userInfo = out.getUserInfo();
//				String host = out.getHost();
//				String query = out.getQuery();
//
//				if (scheme != null) {
//					scheme = scheme.intern();
//				}
//				if (userInfo != null) {
//					userInfo = userInfo.intern();
//				}
//				if (host != null) {
//					host = host.intern();
//				}
//				if (query != null) {
//					query = query.intern();
//				}
//
//				// Rebuild the URI with interned components
//				URI newOut = new URI(
//						scheme,
//						userInfo,
//						host,
//						out.getPort(),
//						out.getPath(),
//						query,
//						out.getFragment()
//				);
//				return (X) newOut;
//			}
			return mapper.convertValue(out, returnType);
		}
		catch (Throwable t) {
			throw new RuntimeException("Error deserializing", t);
		}
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		// Nothing to do
	}
}
