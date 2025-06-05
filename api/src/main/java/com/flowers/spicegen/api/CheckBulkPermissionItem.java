package com.flowers.spicegen.api;

import com.flowers.spicegen.api.internal.CheckBulkPermissionItemImpl;

public interface CheckBulkPermissionItem {

  static Builder newBuilder() {
    return new CheckBulkPermissionItemImpl.Builder();
  }

  ObjectRef resource();

  String permission();

  SubjectRef subject();

  interface Builder {
    Builder resource(ObjectRef resource);

    Builder permission(String permission);

    Builder subject(SubjectRef subject);

    CheckBulkPermissionItem build();
  }
}
