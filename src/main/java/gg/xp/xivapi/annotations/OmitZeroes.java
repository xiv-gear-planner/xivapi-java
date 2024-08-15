package gg.xp.xivapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applicable to a list or array of a sheet reference type (e.g. BaseParam on Item).
 * Indicates that any item with a row_id of zero should be omitted from the list entirely.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.METHOD})
public @interface OmitZeroes {
}
