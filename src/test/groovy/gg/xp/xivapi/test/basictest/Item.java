package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.annotations.OmitZeroes;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.annotations.XivApiMetaField;
import gg.xp.xivapi.clienttypes.XivApiObject;
import gg.xp.xivapi.annotations.XivApiRaw;
import gg.xp.xivapi.annotations.XivApiSheet;

import java.util.List;

// Indicate what sheet should be queried
@XivApiSheet("Item")
public interface Item extends XivApiObject, HasIcon {

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

	// flat struct
	// moved to HasIcon to test inheritance
//	Icon getIcon();

	// Not interesting since this is a food item
	@NullIfZero
	ClassJobCategory getClassJobCategory();

	// Test that it does not return null if annotation is not present
	@XivApiField("ClassJobCategory")
	ClassJobCategory getClassJobCategoryNotNull();

	ItemAction getItemAction();

	@XivApiRaw
	int getLevelItem();

	// test auto-conversion from int to boolean, and also 'is' instead of 'get'
	boolean isDesynth();

	default int getNameSize() {
		return getName().length();
	}

	ClassJob getClassJobUse();

	// TODO: allow/test @NullIfZero for list types
	@XivApiField("BaseParam")
	List<BaseParam> getBaseParamList();

	@XivApiField("BaseParam")
	BaseParam[] getBaseParamArray();

	@XivApiField("BaseParam")
	@XivApiRaw
	List<Integer> getBaseParamRawList();

	// Primitive arrays are NOT supported yet
	@XivApiField("BaseParam")
	@XivApiRaw
	Integer[] getBaseParamRawArray();

	@XivApiField("BaseParam")
	@OmitZeroes
	List<BaseParam> getBaseParamListTrunc();

	@XivApiField("BaseParam")
	List<@OmitZeroes BaseParam> getBaseParamListTrunc2();

	@XivApiField("BaseParam")
	@OmitZeroes
	BaseParam[] getBaseParamArrayTrunc();
}
