package gg.xp.xivapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this object is a top-level sheet object.
 * <p>
 * If a value is supplied, then that will become the sheet name. If left with the default value, then the sheet name
 * will be the interface name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XivApiSheet {
	String value() default "";
}
