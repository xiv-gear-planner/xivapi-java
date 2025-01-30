package gg.xp.xivapi.clienttypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GameVersion(@JsonProperty List<String> names) {
}
