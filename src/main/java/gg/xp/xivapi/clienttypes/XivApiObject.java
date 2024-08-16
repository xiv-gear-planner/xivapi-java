package gg.xp.xivapi.clienttypes;

public interface XivApiObject extends XivApiBase {
	int getPrimaryKey();

	int getRowId();

	default boolean isZero() {
		return getRowId() == 0;
	}
}
