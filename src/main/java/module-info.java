module gg.xp.xivapi {
	requires org.slf4j;
	requires com.fasterxml.jackson.databind;
	requires org.jetbrains.annotations;
	requires java.net.http;
	requires org.apache.commons.collections4;
	requires org.apache.httpcomponents.core5.httpcore5;
	requires java.management;
	requires org.apache.commons.lang3;
	exports gg.xp.xivapi;
	exports gg.xp.xivapi.annotations;
	exports gg.xp.xivapi.clienttypes;
	exports gg.xp.xivapi.exceptions;
	exports gg.xp.xivapi.filters;
	exports gg.xp.xivapi.pagination;
	exports gg.xp.xivapi.assets;
	exports gg.xp.xivapi.url;
}