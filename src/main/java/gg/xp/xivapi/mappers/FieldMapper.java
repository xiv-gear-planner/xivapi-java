package gg.xp.xivapi.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.getters.MetaFieldMapper;
import gg.xp.xivapi.mappers.getters.NormalFieldMapper;
import gg.xp.xivapi.mappers.objects.ObjectFieldMapper;

/**
 * Interface for a Xivapi JSON to POJO converter.
 * <p>
 * These are meant to be chained together to achieve a complex mapping.
 * <p>
 * Generally, the top-level JSON and top-level return type are fed into {@link ObjectFieldMapper}.
 * This looks at the provided interface and picks out fields from that.
 * <p>
 * Each of these fields is then sent to something in the {@link gg.xp.xivapi.mappers.getters} package.
 * These are responsible solely for pulling out the actual field. For example, {@link NormalFieldMapper}
 * is for normal fields, while {@link MetaFieldMapper} is for the 'value' and 'row_id' types of fields.
 * <p>
 * Then, it is delegated to {@link AutoValueMapper}, which determines how to map that specific field.
 * If it is a primitive value, it uses {@link BasicValueMapper}. Otherwise, it calls another mapper.
 * For example, if it is a nested sheet object, then it will use another {@link ObjectFieldMapper}, and
 * the process recurses until it is complete.
 *
 * @param <X>
 */
public interface FieldMapper<X> {
	X getValue(JsonNode current, XivApiContext context);

	void buildQueryFields(QueryFieldsBuilder parent);
}
