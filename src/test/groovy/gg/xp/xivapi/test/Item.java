package gg.xp.xivapi.test;

import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiMetaField;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiSheet;

@XivApiSheet("Item")
public interface Item extends XivApiObject {
	// TODO: arrays
	// TODO: flat structs
	// TODO: id
	// TODO: schema version

	// ID will always be auto-filled, but this is declared on XivApiObject so no need to do it here
//	int getId()

	// Should be pulled from row_id
	@XivApiMetaField("row_id")
	int getRowIdAlt();

	// Should be pulled from 'Name'
	String getName();

	// should be pulled from 'Plural'
	@XivApiField("Plural")
	String pluralName();

	@XivApiField("Plural")
	String getPluralName();

	// should be pulled from 'Rarity'
	int getRarity();

	// TODO
	Icon getIcon();

	// Not interesting since this is a food item
	EquipSlotCategory getEquipSlotCategory();

	ItemAction getItemAction();

	@XivApiRaw
	int getLevelItem();

	// test auto-conversion from int to boolean
	boolean getDesynth();

	default int getNameSize() {
		return getName().length();
	}

}
