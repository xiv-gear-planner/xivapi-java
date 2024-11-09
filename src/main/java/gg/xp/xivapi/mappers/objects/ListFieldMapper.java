package gg.xp.xivapi.mappers.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.xp.xivapi.annotations.OmitZeroes;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.impl.XivApiContext;
import gg.xp.xivapi.mappers.AutoValueMapper;
import gg.xp.xivapi.mappers.FieldMapper;
import gg.xp.xivapi.mappers.QueryFieldsBuilder;
import gg.xp.xivapi.mappers.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mapper for list types.
 * <p>
 * This respects the {@link OmitZeroes} annotation, either on the method or the parameter type. If provided, then the
 * list will not contain any elements for which their {@link XivApiObject#isZero()} method returns true. By default,
 * this is when the row_id is zero. Note that using this with a non XivApiObject type will throw an exception.
 *
 * @param <X> The list type (e.g. List of String, not String)
 */
public class ListFieldMapper<X> implements FieldMapper<List<X>> {

	private static final Logger log = LoggerFactory.getLogger(ListFieldMapper.class);
	private final AutoValueMapper<X> innerMapper;
	private final boolean omitZero;

	public ListFieldMapper(Class<List<X>> cls, Method method, Type returnTypeFull, ObjectMapper mapper) {
		if (!cls.equals(List.class)) {
			throw new IllegalArgumentException("Type must be a List<X>, not %s".formatted(cls));
		}
		if (returnTypeFull instanceof ParameterizedType actualType) {
			Type listType = actualType.getActualTypeArguments()[0];
			Class<?> listTypeAsCls = MappingUtils.parameterizedTypeToRawClass(listType);
			omitZero = method.isAnnotationPresent(OmitZeroes.class)
			           || (method.getAnnotatedReturnType() instanceof AnnotatedParameterizedType apt
			               && apt.getAnnotatedActualTypeArguments()[0].isAnnotationPresent(OmitZeroes.class));
			if (omitZero && !XivApiObject.class.isAssignableFrom(listTypeAsCls)) {
				throw new IllegalArgumentException("@OmitZeroes only makes sense when dealing with a sheet object type");
			}
			//noinspection unchecked
			this.innerMapper = new AutoValueMapper<>((Class<X>) listTypeAsCls, method, listType, mapper);
		}
		else {
			throw new RuntimeException("Unexpected type %s for method %s - not a generic list type".formatted(returnTypeFull, returnTypeFull));
		}
	}

	@Override
	public List<X> getValue(JsonNode current, XivApiContext context) {

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
		return Collections.unmodifiableList(out);
	}

	@Override
	public void buildQueryFields(QueryFieldsBuilder parent) {
		parent.markAsArray();
		innerMapper.buildQueryFields(parent);
	}

}
