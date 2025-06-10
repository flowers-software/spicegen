package com.flowers.spicegen.api;

import com.flowers.spicegen.api.internal.LookupSubjectsImpl;
import java.util.function.Function;

public interface LookupSubjects {
  static LookupSubjects.Builder newBuilder() {
    return new LookupSubjectsImpl.Builder();
  }

  String permission();

  ObjectRef resource();

  String subjectRelation();

  Function<String, ObjectRef> subjectFactory();

  default String subjectType() {
    return subjectFactory().apply("").kind();
  }

  interface Builder {
    Builder permission(String permission);

    Builder resource(ObjectRef resource);

    Builder subjectRelation(String subjectRelation);

    Builder subjectFactory(Function<String, ObjectRef> subjectFactory);

    LookupSubjects build();
  }
}
