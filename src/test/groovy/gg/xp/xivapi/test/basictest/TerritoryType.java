package gg.xp.xivapi.test.basictest;

import gg.xp.xivapi.annotations.NullIfZero;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiObject;

public interface TerritoryType extends XivApiObject {
	@NullIfZero
	@XivApiField("ContentFinderCondition")
	ContentFinderCondition getContentFinderConditionNullable();
	@XivApiField("ContentFinderCondition")
	ContentFinderCondition getContentFinderConditionNonNull();

	PlaceName getPlaceName();

}
