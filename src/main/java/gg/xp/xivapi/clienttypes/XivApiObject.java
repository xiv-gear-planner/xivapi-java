package gg.xp.xivapi.clienttypes;

public interface XivApiObject {
	// TODO: this might need a different name. Some sheets have a literal "ID" field
	int getId();

	int getRowId();

	XivApiSchemaVersion getSchemaVersion();
}
