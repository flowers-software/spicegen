package com.flowers.spicegen.api.internal;

import com.flowers.spicegen.api.LookupSubjects;
import com.flowers.spicegen.api.ObjectRef;
import java.util.function.Function;

public record LookupSubjectsImpl(
    String permission,
    ObjectRef resource,
    Function<String, ObjectRef> subjectFactory,
    String subjectRelation)
    implements LookupSubjects {

  private LookupSubjectsImpl(Builder builder) {
    this(builder.permission, builder.resource, builder.subjectFactory, builder.subjectRelation);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder implements LookupSubjects.Builder {
    private String permission;
    private ObjectRef resource;
    private Function<String, ObjectRef> subjectFactory;
    private String subjectRelation;

    @Override
    public Builder permission(String permission) {
      this.permission = permission;
      return this;
    }

    @Override
    public Builder resource(ObjectRef resource) {
      this.resource = resource;
      return this;
    }

    @Override
    public Builder subjectFactory(Function<String, ObjectRef> subjectFactory) {
      this.subjectFactory = subjectFactory;
      return this;
    }

    @Override
    public LookupSubjects.Builder subjectRelation(String subjectRelation) {
      this.subjectRelation = subjectRelation;
      return this;
    }

    @Override
    public LookupSubjects build() {
      if (permission == null || resource == null || subjectFactory == null) {
        throw new IllegalStateException("Permission, resource, and subjectFactory must be set");
      }
      return new LookupSubjectsImpl(this);
    }
  }
}
