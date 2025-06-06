package com.flowers.spicegen.parser.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CaveatParameter(
    @JsonProperty("name") String name, @JsonProperty("type") String type) {}
