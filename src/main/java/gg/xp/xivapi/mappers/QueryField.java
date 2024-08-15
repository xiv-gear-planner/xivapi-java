package gg.xp.xivapi.mappers;

public record QueryField(QueryFieldType type, String name) {

	public static QueryField normalField(String name) {
		return new QueryField(QueryFieldType.Field, name);
	}

	public static QueryField transientField(String name) {
		return new QueryField(QueryFieldType.TransientField, name);
	}

	public QueryField withPrefixPart(String prefix) {
		return new QueryField(type, "%s.%s".formatted(prefix, this.name));
	}

}
