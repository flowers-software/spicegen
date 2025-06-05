package com.flowers.spicegen.spicedbbinding.internal;

import com.authzed.api.v1.ObjectReference;
import com.flowers.spicegen.api.ObjectRef;

public class ObjectReferenceMapper {

  public ObjectReference map(ObjectRef ref) {
    return ObjectReference.newBuilder().setObjectType(ref.kind()).setObjectId(ref.id()).build();
  }
}
