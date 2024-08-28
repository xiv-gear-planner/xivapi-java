# Xivapi Java Client

This is a basic API client for the new (currently beta) xivapi.com.

The most performant way to access the new Xivapi is by specifying which fields you would like to be
included in the response. This works fairly well in languages like TS where the fields provided in
a query can inform the type checking system. However, in languages such as Java, a different approach
is needed.

This client works by having the end user supply an `interface` with the fields they want, along with
annotations dictating the behavior. The API client then queries the API, and uses a Java Proxy to
create an implementation of the interface, with response fields mapped to the interface methods.

## Dependency Info

See [Maven Central](https://central.sonatype.com/artifact/app.xivgear/xivapi-java)

### Gradle

```groovy
dependencies {
	// Be sure to check what the latest version is
	implementation group: 'app.xivgear', name: 'xivapi-java', version: '0.1.2'
}
```

### Maven

```xml
<dependency>
    <groupId>app.xivgear</groupId>
    <artifactId>xivapi-java</artifactId>
    // Be sure to check what the latest version is
    <version>0.1.2</version>
</dependency>
```

## Example

This example is used in the test suite.

### Data Model

First, you need to establish your model as an interface. You need to tell the API client which fields you want by
including them in your interface.

```java
// This annotation controls what sheet to pull from. If not overridden, defaults to the name of the interface.
@XivApiSheet("Item")
// You MUST extend XivApiObject for your top-level model and any nested row objects
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

	// Structs are supported by declaring another interface which extends XivApiStruct instead of XivApiObject
	Icon getIcon();

	// @NullIfZero indicates that if a sheet reference has a value of zero, it should
	// instead return null.
	// To customize the null-ish check, you can override the default boolean isZero() method on the target interface.
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

### Query Single Row by ID

Then, to query for a specific row, simply use getById(class, id):

```java
var client = new XivApiClient();
Item item = client.getById(Item.class, 44096);
```

### Listing

For lists, you can use getListIterator():

```java
var client = new XivApiClient();
// This implements Iterator. It will only load the next page when hasNext() is called and
// there are no more entries in the current page.
XivApiPaginator<Item> itemsIter = client.getListIterator(Item.class);
// Use toList() to dump all entries into a list
List<Item> items = client.getListIterator(Item.class).toList();
// Use stream() to convert this to a stream
Stream<Item> filteredItems = client.getListIterator(Item.class).stream();
// Since the default iterator does not do any prefetch, you can use
// toBufferedIterator(int) and toBufferedStream(int) to add a buffer:
Stream<Item> filteredItems = client.getListIterator(Item.class).toBufferedStream(200);
// A few extra options are supported with the optional second argument for list options
// For example, to change from the default 100 per page to 50:
XivApiPaginator<Item> itemsIter2 = client.getListIterator(Item.class,
        ListOptions.newBuilder().perPage(50).build());
```

### Searching

For searching, use getSearchIterator(). It supports the same functionality as getListIterator(), but also takes
a query:

```java
import static gg.xp.xivapi.filters.SearchFilters.*;

var client = new XivApiClient();
SearchFilter filter = and(
    // 'of' lets you specify the search string directly
    of("LevelItem>500"),
    or(
        not(eq("Name", "Shadowbringer")),
        eq("Desynth", 0),
        // lt (less than), gt (greater than), lte (less than or equal), gte (greater than or equal)
        lt("LevelItem", 650)
        // Other supported filter methods:
        // binary(field, operator, value) supports any other binary operations needed
        // isTrue(field) and isFalse(field) support booleans
        // strPart(field, value)
        // any(field) is used to wrap an array field's name to allow matching on any element in the array
    )
);
XivApiPaginator<Item> itemsIter = client.getSearchIterator(Item.class, filter);
// Same things are supported, see previous section for more details
// Basic non-buffered iter
XivApiPaginator<Item> itemsIter = client.getSearchIterator(Item.class);
// Dump to list
List<Item> items = client.getSearchIterator(Item.class).toList();
// Unbuffered stream
Stream<Item> filteredItems = client.getSearchIterator(Item.class).stream();
// Buffered stream
Stream<Item> filteredItems = client.getSearchIterator(Item.class).toBufferedStream(200);
// Extra options
XivApiPaginator<Item> itemsIter2 = client.getSearchIterator(Item.class,
		ListOptions.newBuilder().perPage(50).build());
```

## Examples

For more examples, you can consult the [tests](src/test/groovy/gg/xp/xivapi/test).
You can also take a look at the [xivgear.app data API](https://github.com/xiv-gear-planner/xivgear-data-api)
as an example of real-world usage. Note that both of these are written in Groovy, however, the API client itself
is written in pure Java and does not have any Groovy dependencies.

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
- Iteration prefetch
- Other forms of iterator (streams, lists, etc)
- Publish package

### Not Complete

- Multiple languages
  - Untested, but in the meantime, you should still be able to query different languages by overriding the field name, e.g. `@XivApiField("Name@lang(jp)")`.
- Querying based on method references (e.g. something like `equals(Item::getName, "My Item")`)
- Multi-sheet searching
- Groovy DSLs

## Debugging

If you have a `XivApiObject` or `XivApiStruct` object, you can use the 
`getMethodValueMap()` method to dump the raw mapping between methods and values which may
help you determine if something has gone wrong.