package com.flowers.spicegen.model;

import com.flowers.spicegen.parser.schema.CaveatNode;
import java.util.List;

public record Schema(List<ObjectDefinition> definitions, List<CaveatNode> caveats) {}
