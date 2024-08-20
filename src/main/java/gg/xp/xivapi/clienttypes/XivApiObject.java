package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder
public interface XivApiObject extends XivApiBase {
	int getPrimaryKey();

	int getRowId();

	default boolean isZero() {
		return getRowId() == 0;
	}
}
