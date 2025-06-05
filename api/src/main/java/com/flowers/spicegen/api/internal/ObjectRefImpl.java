package com.flowers.spicegen.api.internal;

import com.flowers.spicegen.api.ObjectRef;

public record ObjectRefImpl(String kind, String id) implements ObjectRef {

  @Override
  public String toString() {
    return "%s:%s".formatted(kind, id);
  }
}
