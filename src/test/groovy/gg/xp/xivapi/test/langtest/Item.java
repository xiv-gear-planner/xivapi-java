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

	// This DOES NOT work and would likely be a fair bit of effort to fix, because
	// java.lang.reflect.AnnotatedParameterizedType does not extend java.lang.reflect.Type, so
	// it can't be passed around like Type can.
//	@XivApiField("BaseParam")
//	XivApiLangValue<List<@OmitZeroes BaseParam>> getBaseParamListTrunc2();

	@XivApiField("BaseParam")
	@OmitZeroes
	XivApiLangValue<BaseParam[]> getBaseParamArrayTrunc();
	// TODO: add the form of List<XivApiLangValue>[]
}
