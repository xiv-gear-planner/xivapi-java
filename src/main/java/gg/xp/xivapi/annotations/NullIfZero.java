package gg.xp.xivapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applicable to a sheet reference type (i.e. nested object). Indicates that the getter method should return null
 * if the primary key of the item is zero.
 * <p>
 * This can be applied to an interface, indicating that whenever it is used as a return type, a zero value should
 * become null. It can also be applied to the getter itself to control this behavior on a per-field level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NullIfZero {
}
