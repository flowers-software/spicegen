package com.flowers.spicegen.api;

import com.flowers.spicegen.api.internal.CheckPermissionImpl;

public interface CheckPermission {

  static Builder newBuilder() {
    return new CheckPermissionImpl.Builder();
  }

  ObjectRef resource();

  String permission();

  SubjectRef subject();

  Consistency consistency();

  interface Builder {
    Builder resource(ObjectRef resource);

    Builder permission(String permission);

    Builder subject(SubjectRef subject);

    Builder consistency(Consistency consistency);

    CheckPermission build();
  }
}
