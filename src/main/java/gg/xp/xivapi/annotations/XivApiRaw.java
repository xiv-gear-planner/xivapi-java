package gg.xp.xivapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the targeted method should be retrieved from xivapi using the @as(raw) decorator in the query.
 * The field will only contain the resulting value. For example, if I apply this to the "LevelItem" field in "Item",
 * then instead of receiving the entire LevelItem object, I will just receive the number itself.
 * <p>
 * Generally, this only makes sense for numeric and possibly string types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XivApiRaw {
}
