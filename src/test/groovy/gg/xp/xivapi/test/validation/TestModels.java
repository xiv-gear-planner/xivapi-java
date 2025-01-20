package gg.xp.xivapi.test.validation;

import gg.xp.xivapi.annotations.OmitZeroes;
import gg.xp.xivapi.clienttypes.XivApiObject;

import java.util.List;
import java.util.Map;

public class TestModels {
	interface OmitZeroNonSheetMap extends XivApiObject {
		@OmitZeroes
		Map<String, String> getFoo();
	}

	interface OmitZeroNonSheetArray extends XivApiObject {
		@OmitZeroes
		String[] getFoo();
	}

	interface OmitZeroNonSheetList extends XivApiObject {
		@OmitZeroes
		List<String> getFoo();
	}

	interface BadName extends XivApiObject {
		List<String> foo();
	}
}
