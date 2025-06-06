package com.flowers.spicegen.parser.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class SpecificTypeRefNode extends AbstractNode implements Node {

  @JsonProperty("type_name")
  private String typeName;

  @JsonProperty("relation_name")
  private String relationName;

  @JsonProperty("caveat")
  private String caveat;

  public String typeName() {
    return typeName;
  }

  public String relationName() {
    return relationName;
  }

  public String caveat() {
    return caveat;
  }

  @Override
  public String toString() {
    return "SpecificTypeRefNode{"
        + "typeName='"
        + typeName
        + '\''
        + ", relationName='"
        + relationName
        + '\''
        + ", nodeType="
        + nodeType
        + ", caveat="
        + caveat
        + ", children="
        + children
        + '}';
  }
}
