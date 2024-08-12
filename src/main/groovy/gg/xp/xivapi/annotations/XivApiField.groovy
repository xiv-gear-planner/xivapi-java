package gg.xp.xivapi.annotations

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@interface XivApiField {
	String value()
}