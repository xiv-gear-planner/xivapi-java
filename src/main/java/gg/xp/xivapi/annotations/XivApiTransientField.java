package gg.xp.xivapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Like {@link XivApiField}, but operates on the transient fields instead of the normal fields.
 * If left with the default value, then the normal field naming semantics will apply - that is, if you wish to retrieve
 * transient field "Foo", you can either name the method getFoo/isFoo and decorate it with @XivApiTransientField, or
 * name the method whatever you want and decorate it with @XivApiTransientField("Foo").
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XivApiTransientField {
	String value() default "";
}
