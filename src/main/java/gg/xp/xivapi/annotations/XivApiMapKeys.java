package gg.xp.xivapi.annotations;

import org.intellij.lang.annotations.RegExp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation applies to a Map field. It tells the deserializer which keys to include based on a regex.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XivApiMapKeys {
	/**
	 * @return Which keys to include as a regex
	 */
	@RegExp String value();
}
