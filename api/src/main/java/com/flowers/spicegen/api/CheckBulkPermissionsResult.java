package com.flowers.spicegen.api;

public interface CheckBulkPermissionsResult {
  boolean permissionGranted();

  CheckBulkPermissionItem request();

  default String permission() {
    return request().permission();
  }

  default ObjectRef resource() {
    return request().resource();
  }

  default SubjectRef subject() {
    return request().subject();
  }
}
