package com.flowers.spicegen.api.internal;

import com.flowers.spicegen.api.ObjectRef;
import com.flowers.spicegen.api.SubjectRef;
import com.flowers.spicegen.api.UpdateRelationship;

public record UpdateRelationshipImpl(
    ObjectRef resource, String relation, SubjectRef subject, Operation operation)
    implements UpdateRelationship {
  @Override
  public String toString() {
    var res = resource != null ? resource.toString() : "";
    var rel = relation != null ? relation : "";
    var sub = subject != null ? subject.toString() : "";
    return operation.name() + "(" + res + "#" + rel + "@" + sub + ")";
  }
}
