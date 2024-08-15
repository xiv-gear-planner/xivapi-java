package gg.xp.xivapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Override the default field name to map to.
 * <p>
 * In order to map an interface method to the xivapi field "Foo", you can either:
 * <ul>
 *     <li>Name the method getFoo() or isFoo()</li>
 *     <li>Apply the annotation @XivApiField("Foo") to the method</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XivApiField {
	String value();
}
