package gg.xp.xivapi.annotations

import groovy.transform.CompileStatic

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@CompileStatic
@interface XivApiSheet {
	String value()
}