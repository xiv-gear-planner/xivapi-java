package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public interface XivApiSchemaVersion extends Serializable {

	@JsonIgnore // TODO: make this deserialize better
	String fullVersionString();

}
