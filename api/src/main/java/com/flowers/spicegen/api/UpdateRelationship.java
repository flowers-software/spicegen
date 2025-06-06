package com.flowers.spicegen.api;

import com.flowers.spicegen.api.internal.UpdateRelationshipImpl;
import java.util.Map;

public interface UpdateRelationship {

  static UpdateRelationship ofUpdate(
      ObjectRef resource, String relation, ObjectRef subject, Caveat caveat) {
    return new UpdateRelationshipImpl(
        resource, relation, SubjectRef.ofObject(subject), Operation.UPDATE, caveat);
  }

  static UpdateRelationship ofUpdate(
      ObjectRef resource, String relation, SubjectRef subject, Caveat caveat) {
    return new UpdateRelationshipImpl(resource, relation, subject, Operation.UPDATE, caveat);
  }

  static UpdateRelationship ofDelete(ObjectRef resource, String relation, ObjectRef subject) {
    return new UpdateRelationshipImpl(
        resource, relation, SubjectRef.ofObject(subject), Operation.DELETE, null);
  }

  static UpdateRelationship ofDelete(ObjectRef resource, String relation, SubjectRef subject) {
    return new UpdateRelationshipImpl(resource, relation, subject, Operation.DELETE, null);
  }

  SubjectRef subject();

  ObjectRef resource();

  String relation();

  Operation operation();

  Caveat caveat();

  enum Operation {
    UPDATE,

    DELETE
  }

  record Caveat(String name, Map<String, Object> context) {}
}
