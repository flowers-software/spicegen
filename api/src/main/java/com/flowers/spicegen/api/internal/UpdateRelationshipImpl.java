package com.flowers.spicegen.api.internal;

import com.flowers.spicegen.api.ObjectRef;
import com.flowers.spicegen.api.SubjectRef;
import com.flowers.spicegen.api.UpdateRelationship;
import java.util.Map;

public record UpdateRelationshipImpl(
    ObjectRef resource, String relation, SubjectRef subject, Operation operation, Caveat caveat)
    implements UpdateRelationship {
  @Override
  public String toString() {
    var res = resource != null ? resource.toString() : "";
    var rel = relation != null ? relation : "";
    var sub = subject != null ? subject.toString() : "";
    StringBuilder caveatContextBuilder = new StringBuilder();
    if (caveat != null && !caveat.context().isEmpty()) {
      caveatContextBuilder.append(" with ");
      caveatContextBuilder.append("{");
      for (Map.Entry<String, Object> entry : caveat.context().entrySet()) {
        if (caveatContextBuilder.length() > 7) {
          caveatContextBuilder.append(", ");
        }
        caveatContextBuilder
            .append("\"")
            .append(entry.getKey())
            .append("\": ")
            .append(entry.getValue());
      }
      caveatContextBuilder.append("}");
    }

    return operation.name() + "(" + res + "#" + rel + "@" + sub + ")" + caveatContextBuilder;
  }
}
