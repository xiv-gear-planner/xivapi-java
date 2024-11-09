package gg.xp.xivapi.mappers;

public record QueryField(QueryFieldType type, String name) {

	public static QueryField normalField(String name) {
		return new QueryField(QueryFieldType.Field, name);
	}

	public static QueryField transientField(String name) {
		return new QueryField(QueryFieldType.TransientField, name);
	}

	public QueryField withPrefixPart(QueryFieldType type, String prefix) {
		// If this represents "all", then we can just take the prefix. i.e. instead of 'foo.*', just do 'foo'. The only
		// time we actually want a "*" is at the very top level.
		if (this.isAll()) {
			return new QueryField(type, prefix);
		}
		return new QueryField(type, "%s.%s".formatted(prefix, this.name));
	}

	public static final String ALL = "*";

	public boolean isAll() {
		return ALL.equals(name());
	}

}
