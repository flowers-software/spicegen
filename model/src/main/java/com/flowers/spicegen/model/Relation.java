package com.flowers.spicegen.model;

import java.util.List;

public record Relation(String name, List<ObjectTypeRef> allowedObjects) {}
