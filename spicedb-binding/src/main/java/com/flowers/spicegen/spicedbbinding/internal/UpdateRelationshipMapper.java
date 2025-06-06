package com.flowers.spicegen.spicedbbinding.internal;

import com.authzed.api.v1.ContextualizedCaveat;
import com.authzed.api.v1.Relationship;
import com.authzed.api.v1.RelationshipUpdate;
import com.flowers.spicegen.api.UpdateRelationship;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.Map.Entry;

public class UpdateRelationshipMapper {

  private final ObjectReferenceMapper objectReferenceMapper;
  private final SubjectReferenceMapper subjectReferenceMapper;

  public UpdateRelationshipMapper(
      ObjectReferenceMapper objectReferenceMapper, SubjectReferenceMapper subjectReferenceMapper) {
    this.objectReferenceMapper = objectReferenceMapper;
    this.subjectReferenceMapper = subjectReferenceMapper;
  }

  public RelationshipUpdate map(UpdateRelationship updateRelationship) {

    var subjectRef = subjectReferenceMapper.map(updateRelationship.subject());
    var resourceRef = objectReferenceMapper.map(updateRelationship.resource());

    Relationship.Builder relationshipBuilder =
        Relationship.newBuilder()
            .setRelation(updateRelationship.relation())
            .setSubject(subjectRef)
            .setResource(resourceRef);
    if (updateRelationship.caveat() != null && !updateRelationship.caveat().context().isEmpty()) {

      Struct.Builder structBuilder = Struct.newBuilder();
      for (Entry<String, Object> keyToValue : updateRelationship.caveat().context().entrySet()) {
        switch (keyToValue.getValue()) {
          case String stringValue ->
              structBuilder.putFields(
                  keyToValue.getKey(), Value.newBuilder().setStringValue(stringValue).build());
          case Double doubleValue ->
              structBuilder.putFields(
                  keyToValue.getKey(), Value.newBuilder().setNumberValue(doubleValue).build());
          case Boolean booleanValue ->
              structBuilder.putFields(
                  keyToValue.getKey(), Value.newBuilder().setBoolValue(booleanValue).build());
          default ->
              throw new IllegalArgumentException(
                  "Unsupported value type: " + keyToValue.getValue().getClass());
        }
      }
      relationshipBuilder.setOptionalCaveat(
          ContextualizedCaveat.newBuilder()
              .setCaveatName(updateRelationship.caveat().name())
              .setContext(structBuilder));
    }

    return RelationshipUpdate.newBuilder()
        .setOperation(mapOperation(updateRelationship.operation()))
        .setRelationship(relationshipBuilder)
        .build();
  }

  private RelationshipUpdate.Operation mapOperation(UpdateRelationship.Operation operation) {
    return switch (operation) {
      case UPDATE -> RelationshipUpdate.Operation.OPERATION_TOUCH;
      case DELETE -> RelationshipUpdate.Operation.OPERATION_DELETE;
    };
  }
}
