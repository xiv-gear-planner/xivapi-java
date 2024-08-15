package gg.xp.xivapi.clienttypes;

public interface XivApiObject {
	int getPrimaryKey();

	int getRowId();

	XivApiSchemaVersion getSchemaVersion();

	default boolean isZero() {
		return getRowId() == 0;
	}
}
