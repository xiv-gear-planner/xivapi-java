package gg.xp.xivapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similar to {@link XivApiField}, but indicates that the value comes from a top level field outside the "fields"
 * object. For example, "row_id", "value", or "score".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XivApiMetaField {
	String value();
}
