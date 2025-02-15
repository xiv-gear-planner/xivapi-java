package gg.xp.xivapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that instead of reading from a field, the current object itself should be used.
 * <p>
 * That is, if I have a method called "getFoo()", by default, it will look at './fields/Foo'. If this annotation
 * is used, then it will instead deserialize '.'.
 * <p>
 * This annotation implies that all fields on this object should be retrieved, i.e. {@code fields=*} (or {@code transient=*}).
 * At the top level, this can NOT be combined with fields that would require an annotation such as @raw or @lang,
 * but this is possible within nested objects because you could request them separately.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XivApiThis {
}
