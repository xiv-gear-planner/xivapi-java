package gg.xp.xivapi.mappers.getters;

import com.fasterxml.jackson.databind.JsonNode;
import gg.xp.xivapi.assets.AssetFormat;
import gg.xp.xivapi.clienttypes.XivApiAsset;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.url.XivApiUrlResolver;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;

public class AssetFieldMapper<X extends AssetFormat> implements FieldMapper<XivApiAsset<X>> {

	@Override
	public XivApiAsset<X> getValue(JsonNode current, XivApiContext context) {
		try {
			String raw = current.textValue();
			return new XivApiAssetImpl<>(raw, context.urlResolver());
		}
		catch (Throwable t) {
			throw new RuntimeException("Error deserializing", t);
		}
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		// Nothing to do
	}

	private record XivApiAssetImpl<X extends AssetFormat>(String raw,
	                                                      XivApiUrlResolver resolver) implements XivApiAsset<X>, Serializable {

		@Serial
		private static final long serialVersionUID = 4701049157502496553L;

		@Override
		public URI getURI(String formatRaw) {
			return resolver.getAssetUri(raw, formatRaw);
		}

		@Override
		public URI getURI(X format) {
			return resolver.getAssetUri(raw, format);
		}
	}
}
