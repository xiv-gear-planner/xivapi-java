package gg.xp.xivapi.test.langtest;

import gg.xp.xivapi.annotations.OmitZeroes;
import gg.xp.xivapi.annotations.XivApiField;
import gg.xp.xivapi.clienttypes.XivApiLangValue;
import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.List;

public interface Item extends XivApiObject {

	@XivApiField("BaseParam")
	XivApiLangValue<List<BaseParam>> getBaseParamList();

	@XivApiField("BaseParam")
	XivApiLangValue<BaseParam[]> getBaseParamArray();

	@XivApiField("BaseParam")
	@OmitZeroes
	XivApiLangValue<List<BaseParam>> getBaseParamListTrunc();

	@XivApiField("BaseParam")
	XivApiLangValue<List<@OmitZeroes BaseParam>> getBaseParamListTrunc2();

	@XivApiField("BaseParam")
	@OmitZeroes
	XivApiLangValue<BaseParam[]> getBaseParamArrayTrunc();
	// TODO: add the form of List<XivApiLangValue>[]
}
