package com.flowers.spicegen.api.internal;

import com.flowers.spicegen.api.CheckPermission;
import com.flowers.spicegen.api.Consistency;
import com.flowers.spicegen.api.ObjectRef;
import com.flowers.spicegen.api.SubjectRef;

public record CheckPermissionImpl(
    ObjectRef resource, String permission, SubjectRef subject, Consistency consistency)
    implements CheckPermission {

  private CheckPermissionImpl(Builder builder) {
    this(builder.resource, builder.permission, builder.subject, builder.consistency);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder implements CheckPermission.Builder {
    private ObjectRef resource;
    private String permission;
    private SubjectRef subject;
    private Consistency consistency = Consistency.fullyConsistent();

    @Override
    public Builder resource(ObjectRef resource) {
      this.resource = resource;
      return this;
    }

    @Override
    public Builder permission(String permission) {
      this.permission = permission;
      return this;
    }

    @Override
    public Builder subject(SubjectRef subject) {
      this.subject = subject;
      return this;
    }

    @Override
    public Builder consistency(Consistency consistency) {
      this.consistency = consistency;
      return this;
    }

    @Override
    public CheckPermission build() {
      return new CheckPermissionImpl(this);
    }
  }
}
