package gg.xp.xivapi.collections;

import java.io.Serializable;

public interface KeySerDe<K, S extends Serializable> extends Serializable {
	S toSerializableForm(K key);
	K fromSerializeableForm(S serializable);
}
