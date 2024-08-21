package gg.xp.xivapi.mappers;

/**
 * Type of a field
 */
public enum QueryFieldType {
	/**
	 * Normal field, i.e. in 'fields' in the xivapi response
	 */
	Field,
	/**
	 * Transient field, i.e. in 'transient' in the xivapi response
	 */
	TransientField
}
