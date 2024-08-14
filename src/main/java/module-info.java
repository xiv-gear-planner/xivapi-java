module gg.xp.xivapi {
	requires org.slf4j;
	requires com.fasterxml.jackson.databind;
	requires org.jetbrains.annotations;
	requires java.net.http;
	exports gg.xp.xivapi;
	exports gg.xp.xivapi.annotations;
	exports gg.xp.xivapi.clienttypes;
	exports gg.xp.xivapi.debug;
	exports gg.xp.xivapi.exceptions;
}