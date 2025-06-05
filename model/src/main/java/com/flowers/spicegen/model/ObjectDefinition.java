package com.flowers.spicegen.model;

import java.util.List;

public record ObjectDefinition(
    String name, List<Relation> relations, List<Permission> permissions) {}
