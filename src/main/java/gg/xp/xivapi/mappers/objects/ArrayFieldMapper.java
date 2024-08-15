package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.OmitZeroes;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryField;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for array types.
 * <p>
 * This respects the {@link OmitZeroes} annotation on the method. If provided, then the
 * array will not contain any elements for which their {@link XivApiObject#isZero()} method returns true. By default,
 * this is when the row_id is zero. Note that using this with a non XivApiObject type will throw an exception.
 *
 * @param <X> The list type (e.g. List of String, not String)
 */
public class ArrayFieldMapper<X> implements FieldMapper<X[]> {

	private static final Logger log = LoggerFactory.getLogger(ArrayFieldMapper.class);
	private final AutoValueMapper<X> innerMapper;
	private final Class<X> componentType;
	private final boolean omitZero;

	@SuppressWarnings("unchecked")
	public ArrayFieldMapper(Class<X[]> cls, Method method, Type returnTypeFull, ObjectMapper mapper) {
		if (!cls.isArray()) {
			throw new IllegalArgumentException("Type must be an array, not %s".formatted(cls));
		}
		// TODO: this doesn't support generic types
		Class<?> listTypeAsCls = cls.componentType();
		this.componentType = (Class<X>) listTypeAsCls;
		omitZero = method.isAnnotationPresent(OmitZeroes.class);
		if (omitZero && !XivApiObject.class.isAssignableFrom(listTypeAsCls)) {
			throw new IllegalArgumentException("@OmitZeroes only makes sense when dealing with a sheet object type");
		}
		//noinspection unchecked
		this.innerMapper = new AutoValueMapper<>((Class<X>) listTypeAsCls, method, listTypeAsCls, mapper);
	}

	@SuppressWarnings("unchecked")
	@Override
	public X[] getValue(JsonNode current, XivApiContext context) {

		if (!current.isArray()) {
			throw new IllegalArgumentException("Expected an array, got %s".formatted(current));
		}

		List<X> out = new ArrayList<>(current.size());
		for (JsonNode jsonNode : current) {
			X value = innerMapper.getValue(jsonNode, context);
			if (omitZero) {
				if (((XivApiObject) value).isZero()) {
					continue;
				}
			}
			out.add(value);
		}

		X[] array = (X[]) Array.newInstance(componentType, out.size());

		for (int i = 0; i < out.size(); i++) {
			array[i] = out.get(i);
		}

		return array;
	}

	@Override
	public List<QueryField> getQueryFields() {
		return innerMapper.getQueryFields();
	}

}
