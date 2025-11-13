package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameVersion(@JsonProperty List<String> names, @JsonProperty String key) {
}
