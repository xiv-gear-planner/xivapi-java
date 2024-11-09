package gg.xp.xivapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicate that the field should be retrieved with the given language.
 * <p>
 * For example, applying {@code @XivApiLang("de")} will request the German version of the string. Equivalent to the
 * {@code @lang(de)} decorator.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XivApiLang {
	String value();
}
