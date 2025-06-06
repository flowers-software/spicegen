package com.flowers.spicegen.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowers.spicegen.model.*;
import com.flowers.spicegen.parser.schema.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiceDbSchemaParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpiceDbSchemaParser.class);

  public Schema parse(Path schema) {

    Path astPath = null;
    try {

      var preprocessor = new AstPreProcessor();

      var name = schema.getFileName().toString();
      name = name.substring(0, name.lastIndexOf('.'));

      astPath = Files.createTempFile("%s_ast_".formatted(name), ".json");

      LOGGER.info("pre-processing schema into AST from {} to {}", schema, astPath);
      preprocessor.parse(astPath, schema);

      LOGGER.info("loading AST from {}", astPath);
      var root = loadAst(astPath);

      LOGGER.debug("parsing schema from AST");
      var definitions =
          streamNullable(root.unwrap(BaseNode.class).children())
              .filter(byKind(NodeType.NodeTypeDefinition))
              .map(d -> d.unwrap(DefinitionNode.class))
              .map(this::mapDefinition)
              .toList();

      var caveats =
          streamNullable(root.unwrap(BaseNode.class).children())
              .filter(byKind(NodeType.NodeTypeCaveatDefinition))
              .map(d -> d.unwrap(CaveatNode.class))
              .toList();

      LOGGER.debug("schema parsed");

      return new Schema(definitions, caveats);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } finally {
      deleteQuietly(astPath);
    }
  }

  private Node loadAst(Path astPath) {

    try (var is = Files.newInputStream(astPath)) {
      var root = tryParseAst(is);
      if (root.kind() != NodeType.NodeTypeFile) {
        throw new IllegalArgumentException(
            "unexpected root node: %s".formatted(root.kind().name()));
      }
      return root;
    } catch (IOException e) {
      throw new IllegalStateException("failed to load AST from '%s'".formatted(astPath), e);
    }
  }

  private void deleteQuietly(Path p) {
    if (p == null) {
      return;
    }

    try {
      Files.deleteIfExists(p);
    } catch (IOException e) {
      // ignore quietly
    }
  }

  private ObjectDefinition mapDefinition(DefinitionNode n) {
    var relations =
        streamNullable(n.children())
            .filter(byKind(NodeType.NodeTypeRelation))
            .map(node -> node.unwrap(RelationNode.class))
            .map(this::mapRelation)
            .toList();

    var permissions =
        streamNullable(n.children())
            .filter(byKind(NodeType.NodeTypePermission))
            .map(node -> node.unwrap(PermissionNode.class))
            .map(this::mapPermission)
            .toList();

    return new ObjectDefinition(n.name(), relations, permissions);
  }

  private Permission mapPermission(PermissionNode n) {
    return new Permission(n.name());
  }

  private Relation mapRelation(RelationNode n) {

    var allowedTypes =
        streamNullable(n.allowedTypes())
            .filter(byKind(NodeType.NodeTypeTypeReference))
            .map(node -> node.unwrap(TypeRefNode.class))
            .flatMap(t -> t.typeRefTypes().stream())
            .filter(byKind(NodeType.NodeTypeSpecificTypeReference))
            .map(SpecificTypeRefNode.class::cast)
            .map(node -> new ObjectTypeRef(node.typeName(), node.relationName(), node.caveat()))
            .toList();

    return new Relation(n.name(), allowedTypes);
  }

  private static Predicate<Node> byKind(NodeType kind) {
    return n -> n.kind() == kind;
  }

  private static <T> Stream<T> streamNullable(Collection<T> t) {
    return Optional.ofNullable(t).stream().flatMap(Collection::stream);
  }

  private Node tryParseAst(InputStream is) throws IOException {

    var om = new ObjectMapper();
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return om.readValue(is, Node.class);
  }
}
