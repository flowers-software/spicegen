package com.flowers.spicegen.spicedbbinding;

import com.authzed.api.v1.PermissionsServiceGrpc;
import com.flowers.spicegen.api.PermissionService;
import com.flowers.spicegen.spicedbbinding.internal.SpiceDbPermissionServiceImpl;

public class SpiceDbPermissionServiceBuilder {
  private PermissionsServiceGrpc.PermissionsServiceBlockingStub stub;

  public static SpiceDbPermissionServiceBuilder newBuilder() {
    return new SpiceDbPermissionServiceBuilder();
  }

  private SpiceDbPermissionServiceBuilder() {}

  public SpiceDbPermissionServiceBuilder permissionsBlockingStub(
      PermissionsServiceGrpc.PermissionsServiceBlockingStub stub) {
    this.stub = stub;
    return this;
  }

  public PermissionService build() {
    return new SpiceDbPermissionServiceImpl(stub);
  }
}
