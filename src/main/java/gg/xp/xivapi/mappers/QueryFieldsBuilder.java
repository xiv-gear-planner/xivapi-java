package gg.xp.xivapi.mappers;

import gg.xp.xivapi.annotations.XivApiThis;
import gg.xp.xivapi.exceptions.XivApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed class QueryFieldsBuilder permits RootQueryFieldsBuilder {

	protected final String name;
	protected boolean isTransient;
	protected boolean isArray;
	protected final List<String> decorators;
	protected final List<QueryFieldsBuilder> children;

	protected QueryFieldsBuilder(String name) {
		this(name, false, false, List.of(), List.of());
	}

	protected QueryFieldsBuilder(String name, boolean isTransient, boolean isArray, List<String> decorators, List<QueryFieldsBuilder> children) {
		this.name = name;
		this.isTransient = isTransient;
		this.isArray = isArray;
		this.decorators = new ArrayList<>(decorators);
		this.children = new ArrayList<>(children);
	}

	/**
	 * @return A QueryFieldsBuilder that represents a wildcard, i.e. when using {@link XivApiThis}.
	 */
	public static QueryFieldsBuilder all() {
		return new QueryFieldsBuilder(ALL);
	}

	/**
	 * @param name The field name
	 * @return A QueryFieldsBuilder that represents the named field.
	 */
	public static QueryFieldsBuilder normalField(String name) {
		return new QueryFieldsBuilder(name, false, false, new ArrayList<>(), new ArrayList<>());
	}

	/**
	 * Mark this field as being a transient. Note that this will fail later if this is not a top-level field.
	 */
	public void markAsTransient() {
		isTransient = true;
	}

	/**
	 * Mark this field as being an array.
	 */
	public void markAsArray() {
		isArray = true;
	}

	/**
	 * Add a decorator to this field
	 *
	 * @param decorator The decorator, without the '@'. e.g. {@code @lang(en)} would be {@code "lang(en)"}.
	 */
	public void addDecorator(String decorator) {
		decorators.add(decorator);
	}

	/**
	 * Add a child field to this field
	 *
	 * @param child The child field
	 */
	public void addChild(QueryFieldsBuilder child) {
		children.add(child);
	}

	/**
	 * Placeholder value used for wildcard fields. It only becomes a literal "*" for top-level wildcards, but that
	 * logic is handled in {@link RootQueryFieldsBuilder#formatQueryFields()} and this value never comes into play.
	 */
	protected static final String ALL = "*";
	/**
	 * Placeholder value used for the root field. Not used anywhere.
	 */
	protected static final String ROOT = "$ROOT$";

	public boolean isAll() {
		return ALL.equals(name);
	}


	@Override
	public String toString() {
		return "QueryField(%s)".formatted(toQueryStrings(false));
	}

	/**
	 * Turn this set of fields into actual strings that can be plugged into the 'fields' or 'transient' parameter
	 *
	 * @param isTopLevel true if this is a "top level" field, i.e. it is not a child of any other object.
	 * @return A list of strings representing this query (and children).
	 */
	public List<String> toQueryStrings(boolean isTopLevel) {
		if (children.isEmpty() && isAll()) {
			// If no children, and this item is a wildcard itself, then just use the parent, i.e.
			// return nothing. Unless this is top-level, in which case we return a literal '*' which is
			// an undocumented shorthand for 'all fields'.
			// However, we do not want to *replace* other fields, neither at the top level nor nested,
			// because individually-requested fields may still have decorators that need to be respected.
			if (isTopLevel) {
				return List.of("*");
			}
			else {
				return List.of();
			}
		}
		else {
			StringBuilder sb = new StringBuilder();
			// Style is FieldName@decorator(foo)@decorator(bar)[]
			// Name first
			sb.append(name);
			// Decorators second
			decorators.forEach(deco -> sb.append('@').append(deco));
			// Array marker last
			if (isArray) {
				sb.append("[]");
			}
			String value = sb.toString();
			// Now, we need to combine this with children
			Set<String> out = children.stream().flatMap(child -> {
						if (child.isTransient && !isTopLevel) {
							throw new XivApiException("Cannot have a transient field '%s' in a nested object".formatted(child.name));
						}
						// For a wildcard nested object, you don't use *. You just specify the nested
						// field with no further children, e.g. `fields=ClassJobCategory`
						if (child.isAll()) {
							return Stream.of(value);
						}
						// For normal children, render out their query strings, and use a . to separate them.
						return child.toQueryStrings(false).stream()
								.map(childQs -> value + '.' + childQs);
					})
					.collect(Collectors.toSet());

			// If there were no children, return the parent. This handles the childless case.
			if (out.isEmpty()) {
				return List.of(value);
			}
			return new ArrayList<>(out);
		}
	}


}
