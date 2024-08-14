package gg.xp.xivapi.clienttypes;

public interface XivApiObject {
	int getPrimaryKey();

	int getRowId();

	XivApiSchemaVersion getSchemaVersion();
}
