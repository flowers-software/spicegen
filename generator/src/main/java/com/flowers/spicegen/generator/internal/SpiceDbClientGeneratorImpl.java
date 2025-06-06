package com.flowers.spicegen.generator.internal;

import static com.flowers.spicegen.generator.utils.TextUtils.toPascalCase;

import com.flowers.spicegen.api.CheckBulkPermissionItem;
import com.flowers.spicegen.api.CheckPermission;
import com.flowers.spicegen.api.Consistency;
import com.flowers.spicegen.api.ObjectRef;
import com.flowers.spicegen.api.SubjectRef;
import com.flowers.spicegen.api.UpdateRelationship;
import com.flowers.spicegen.api.UpdateRelationship.Caveat;
import com.flowers.spicegen.generator.Options;
import com.flowers.spicegen.generator.SpiceDbClientGenerator;
import com.flowers.spicegen.generator.utils.TextUtils;
import com.flowers.spicegen.model.ObjectDefinition;
import com.flowers.spicegen.model.ObjectTypeRef;
import com.flowers.spicegen.model.Permission;
import com.flowers.spicegen.model.Relation;
import com.flowers.spicegen.model.Schema;
import com.flowers.spicegen.parser.schema.CaveatNode;
import com.flowers.spicegen.parser.schema.CaveatParameter;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeSpec.Builder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public class SpiceDbClientGeneratorImpl implements SpiceDbClientGenerator {

  private static final String REFS_PACKAGE = ".refs";
  private final TypeName objectRefTypeName = ClassName.get(ObjectRef.class);
  private final TypeName updateRelationshipTypeName = ClassName.get(UpdateRelationship.class);

  private final Options options;

  public SpiceDbClientGeneratorImpl(Options options) {
    this.options = options;
  }

  private TypeSpecStore typeSpecStore;

  @Override
  public void generate(Schema spec) {
    typeSpecStore = new TypeSpecStore();
    generateConstants(spec);
    generateRefs(spec);
  }

  private void generateConstants(Schema spec) {

    TypeSpec.Builder constants =
        TypeSpec.classBuilder("SchemaConstants").addModifiers(Modifier.PUBLIC);

    for (ObjectDefinition definition : spec.definitions()) {
      FieldSpec namespace =
          FieldSpec.builder(String.class, "NAMESPACE_" + definition.name().toUpperCase())
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
              .initializer("\"" + definition.name() + "\"")
              .build();
      constants.addField(namespace);

      for (Permission permission : definition.permissions()) {
        FieldSpec permissionField =
            FieldSpec.builder(
                    String.class,
                    "PERMISSION_"
                        + definition.name().toUpperCase()
                        + "_"
                        + permission.name().toUpperCase())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"" + permission.name() + "\"")
                .build();
        constants.addField(permissionField);
      }

      for (Relation relations : definition.relations()) {
        FieldSpec relationshipField =
            FieldSpec.builder(
                    String.class,
                    "RELATIONSHIP_"
                        + definition.name().toUpperCase()
                        + "_"
                        + relations.name().toUpperCase())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"" + relations.name() + "\"")
                .build();
        constants.addField(relationshipField);
      }
    }

    var constantsClass = constants.build();
    if (typeSpecStore.has(constantsClass.name())) {
      return;
    }
    typeSpecStore.put(constantsClass);
    writeSource(constantsClass, "");
  }

  private void generateRefs(Schema spec) {

    for (ObjectDefinition definition : spec.definitions()) {
      var className = TextUtils.capitalize(TextUtils.toCamelCase(definition.name())) + "Ref";
      var typedRef = TypeSpec.recordBuilder(className).build();
      if (typeSpecStore.has(typedRef.name())) {
        return;
      }
      typeSpecStore.put(typedRef);

      var typedRefBuilder =
          typedRef.toBuilder()
              .addSuperinterface(objectRefTypeName)
              .addModifiers(Modifier.PUBLIC)
              .addField(
                  FieldSpec.builder(
                          String.class, "KIND", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                      .initializer("$S", definition.name())
                      .build())
              .recordConstructor(
                  MethodSpec.constructorBuilder()
                      .addParameter(ParameterSpec.builder(String.class, "id").build())
                      .build())
              .addMethod(
                  MethodSpec.methodBuilder("kind")
                      .addModifiers(Modifier.PUBLIC)
                      .returns(String.class)
                      .addCode(CodeBlock.builder().addStatement("return KIND").build())
                      .build())
              .addMethod(
                  MethodSpec.methodBuilder("ofUuid")
                      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                      .returns(ClassName.bestGuess(className))
                      .addParameter(UUID.class, "id")
                      .addCode(
                          """
                              if (id == null) {
                               throw new IllegalArgumentException("id must not be null");
                              }
                              return new $T(id.toString());""",
                          ClassName.bestGuess(className))
                      .build())
              .addMethod(
                  MethodSpec.methodBuilder("ofLong")
                      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                      .returns(ClassName.bestGuess(className))
                      .addParameter(TypeName.LONG, "id")
                      .addCode("return new $T(String.valueOf(id));", ClassName.bestGuess(className))
                      .build())
              .addMethod(
                  MethodSpec.methodBuilder("of")
                      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                      .returns(ClassName.bestGuess(className))
                      .addParameter(String.class, "id")
                      .addCode(
                          """
                              if (id == null) {
                               throw new IllegalArgumentException("id must not be null");
                              }
                              return new $T(id);""",
                          ClassName.bestGuess(className))
                      .build());
      Map<String, CaveatNode> caveats =
          spec.caveats().stream().collect(Collectors.toMap(CaveatNode::name, Function.identity()));
      addUpdateMethods(typedRefBuilder, definition, caveats);

      addCheckMethods(typedRefBuilder, definition);
      addBulkCheckMethods(typedRefBuilder, definition);

      typedRef = typedRefBuilder.build();
      writeSource(typedRef, REFS_PACKAGE);
    }
  }

  private void addCheckMethods(TypeSpec.Builder typeRefBuilder, ObjectDefinition definition) {
    for (Permission permission : definition.permissions()) {

      var permissionName = TextUtils.toPascalCase(permission.name());
      var checkMethod = "check" + permissionName;

      var subjectParamName = "subject";
      var consistencyParamName = "consistency";

      typeRefBuilder.addMethod(
          MethodSpec.methodBuilder(checkMethod)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ClassName.get(SubjectRef.class), subjectParamName)
              .addParameter(ClassName.get(Consistency.class), consistencyParamName)
              .returns(ClassName.get(CheckPermission.class))
              .addCode(
                  """
                if ($L == null) {
                 throw new IllegalArgumentException("subject must not be null");
                }
                return CheckPermission.newBuilder().resource(this).permission($S).subject($L).consistency($L).build();
              """,
                  subjectParamName,
                  permission.name(),
                  subjectParamName,
                  consistencyParamName)
              .build());
    }
  }

  private void addBulkCheckMethods(TypeSpec.Builder typeRefBuilder, ObjectDefinition definition) {
    for (Permission permission : definition.permissions()) {

      var permissionName = TextUtils.toPascalCase(permission.name());
      var checkMethod = "checkBulk" + permissionName;

      var subjectParamName = "subject";

      typeRefBuilder.addMethod(
          MethodSpec.methodBuilder(checkMethod)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ClassName.get(SubjectRef.class), subjectParamName)
              .returns(ClassName.get(CheckBulkPermissionItem.class))
              .addCode(
                  """
                if ($L == null) {
                 throw new IllegalArgumentException("subject must not be null");
                }
                return CheckBulkPermissionItem.newBuilder().resource(this).permission($S).subject($L).build();
              """,
                  subjectParamName,
                  permission.name(),
                  subjectParamName)
              .build());
    }
  }

  private void addUpdateMethods(
      Builder typeRefBuilder, ObjectDefinition definition, Map<String, CaveatNode> caveats) {

    for (Relation relation : definition.relations()) {

      var relationCamelCase = TextUtils.toPascalCase(relation.name());

      for (ObjectTypeRef allowedObject : relation.allowedObjects()) {
        var relationshipName =
            allowedObject.relationship() == null ? "" : toPascalCase(allowedObject.relationship());
        // add create
        var createMethod =
            "create%s%s%s"
                .formatted(
                    relationCamelCase,
                    TextUtils.toPascalCase(allowedObject.typeName()),
                    relationshipName);

        // TODO magic ref
        var typeRefName = toPascalCase(allowedObject.typeName()) + "Ref";

        String caveat = allowedObject.caveat();
        MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder(createMethod);
        createMethodBuilder
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get(options.packageName() + REFS_PACKAGE, typeRefName), "ref")
            .returns(updateRelationshipTypeName);
        CodeBlock.Builder codeblockBuilder =
            CodeBlock.builder()
                .add(
                    """
                  if ($L == null) {
                   throw new IllegalArgumentException("ref must not be null");
                  }
                  $T<String, Object> caveatContext = new $T<>();
                  """,
                    "ref",
                    Map.class,
                    HashMap.class);
        if (caveat != null) {
          CaveatNode caveatNode = caveats.get(caveat);
          for (CaveatParameter parameter : caveatNode.parameters()) {
            Class<?> parameterType =
                switch (parameter.type()) {
                  case "string" -> String.class;
                  case "int" -> Double.class;
                  case "uint" -> Double.class;
                  case "double" -> Double.class;
                  case "boolean" -> Boolean.class;
                  default -> Object.class;
                };
            createMethodBuilder.addParameter(ClassName.get(parameterType), parameter.name());
            codeblockBuilder.add(
                """
                    if ($L != null) {
                     caveatContext.put($S, $L);
                    }
                    """,
                parameter.name(),
                parameter.name(),
                parameter.name());
          }
          codeblockBuilder.add(
              """
                  return $T.ofUpdate(this, $S, $T.ofObjectWithRelation($L, $S), new $T($S, caveatContext));
                  """,
              updateRelationshipTypeName,
              relation.name(),
              SubjectRef.class,
              "ref",
              allowedObject.relationship(),
              Caveat.class,
              allowedObject.caveat());
          createMethodBuilder.addCode(codeblockBuilder.build());
        } else {
          createMethodBuilder.addCode(
              """
                  if ($L == null) {
                   throw new IllegalArgumentException("ref must not be null");
                  }
                  return $T.ofUpdate(this, $S, $T.ofObjectWithRelation($L, $S), null);
                  """,
              "ref",
              updateRelationshipTypeName,
              relation.name(),
              SubjectRef.class,
              "ref",
              allowedObject.relationship());
        }
        typeRefBuilder.addMethod(createMethodBuilder.build());

        var deleteMethod =
            "delete%s%s%s"
                .formatted(
                    relationCamelCase,
                    TextUtils.toPascalCase(allowedObject.typeName()),
                    relationshipName);

        typeRefBuilder.addMethod(
            MethodSpec.methodBuilder(deleteMethod)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(
                    ClassName.get(options.packageName() + REFS_PACKAGE, typeRefName), "ref")
                .returns(ClassName.bestGuess("UpdateRelationship"))
                .addCode(
                    """
                        if ($L == null) {
                         throw new IllegalArgumentException("ref must not be null");
                        }
                        return $T.ofDelete(this, $S, SubjectRef.ofObjectWithRelation($L, $S));
                        """,
                    "ref",
                    updateRelationshipTypeName,
                    relation.name(),
                    "ref",
                    allowedObject.relationship())
                .build());
      }
    }
  }

  private void writeSource(TypeSpec typeSpec, String subpackage) {
    var outputDirectory = options.outputDirectory();
    var packageName = options.packageName() + subpackage;
    try {
      var path = Path.of(outputDirectory);

      Files.createDirectories(path);

      JavaFile.builder(packageName, typeSpec).build().writeTo(path);
    } catch (IOException e) {
      throw new IllegalStateException("cannot write sources to: " + outputDirectory, e);
    }
  }
}
