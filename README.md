# Xivapi Java Client

This is a WIP Xivapi.com Java client.

The most performant way to access the new Xivapi is by specifying which fields you would like to be
included in the response. This works fairly well in languages like TS where the fields provided in
a query can inform the type checking system. However, in languages such as Java, a different approach
is needed.

This client works by having the end user supply an `interface` with the fields they want, along with
annotations dictating the behavior. The API client then queries the API, and uses a Java Proxy to
create an implementation of the interface, with response fields mapped to the interface methods.

## Example

This example is used in the test suite.

```java

// This annotation controls what sheet to pull from.
@XivApiSheet("Item")
public interface Item extends XivApiObject {

	// While value/row_id are already available via XivApiObject, this shows how to query
	// these items which are outside of the 'fields' object using the @XivApiMetaField
	// annotation.
	@XivApiMetaField("row_id")
	int getRowIdAlt();

	// Based on the method name, this will pull from the 'Name' field on the response.
	String getName();

	// You can override the field to pull from using the @XivApiField annotation.
	@XivApiField("Plural")
	String pluralName();

	@XivApiField("Plural")
	String getPluralName();

	// Primitive types are supported
	int getRarity();

	// References to other sheets are supported by declaring another interface which extends XivApiObject
	ItemAction getItemAction();

	// Structs are supported by declaring another interface which extends XivApiStruct
	Icon getIcon();

	// @NullIfZero indicates that if a sheet reference has a value of zero, it should
	// instead return null.
	@NullIfZero
	ClassJobCategory getClassJobCategory();

	// To retrieve just the ID of a cross-sheet reference, i.e. @as(raw), use the @XivApiRaw annotation.
	@XivApiRaw
	int getLevelItem();

	// Some auto-conversions, such as int -> boolean, are supported
	boolean isDesynth();

	// Interface default methods work as you would expect
	default int getNameSize() {
		return getName().length();
	}

	ClassJob getClassJobUse();

}
```

Then, to query for a specific item, simply do:

```java
var client = new XivApiClient();
Item item = client.getById(Item.class, 44096);
```


## Status

### Working

- Single item querying
- Primitive, cross-sheet object, and struct
- Interface default methods
- as(raw)
- Iterating over a sheet
- Basic searching
- Transients
- List and array types
- Schema versions
- Cache mappings so they don't have to be recomputed

### Not Complete

- Multiple languages
- Querying based on method references (e.g. something like  `equals(Item::getName, "My Item")`)
- Multi-sheet searching
- Iteration prefetch
- Other forms of iterator (streams, lists, etc)
- Groovy DSLs
- Publish package