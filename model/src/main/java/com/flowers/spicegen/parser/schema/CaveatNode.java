package com.flowers.spicegen.parser.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public final class CaveatNode extends AbstractNode implements Node {

  @JsonProperty("name")
  private String name;

  @JsonProperty("parameters")
  private List<CaveatParameter> parameters;

  public String name() {
    return name;
  }

  public List<CaveatParameter> parameters() {
    return parameters;
  }
}
