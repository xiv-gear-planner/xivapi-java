package gg.xp.xivapi.mappers.objects.serialization;

import gg.xp.xivapi.collections.KeySerDe;

import java.io.Serial;
import java.lang.reflect.Method;

public class MethodKeySerDe implements KeySerDe<Method, MethodMetadata> {
	@Serial
	private static final long serialVersionUID = 2L;

	@Override
	public MethodMetadata toSerializableForm(Method key) {
		return MethodMetadata.fromMethod(key);
	}

	@Override
	public Method fromSerializeableForm(MethodMetadata serializable) {
		try {
			return serializable.toMethod();
		}
		catch (NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
