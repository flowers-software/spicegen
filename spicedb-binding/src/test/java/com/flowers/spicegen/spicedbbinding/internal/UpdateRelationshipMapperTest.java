package com.flowers.spicegen.spicedbbinding.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.authzed.api.v1.RelationshipUpdate;
import com.flowers.spicegen.api.ObjectRef;
import com.flowers.spicegen.api.UpdateRelationship;
import com.flowers.spicegen.api.UpdateRelationship.Caveat;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UpdateRelationshipMapperTest {

  private static final String TENANT = "tenant";
  private static final String USER = "user";
  private static final String ADMINISTRATOR = "administrator";
  private static final String ID = "9392";

  private final UpdateRelationshipMapper mapper =
      new UpdateRelationshipMapper(new ObjectReferenceMapper(), new SubjectReferenceMapper());

  @Test
  void test_mapper_withUpdateOperation() {

    var resource = ObjectRef.of(TENANT, ID);
    var subject = ObjectRef.of(USER, ID);

    var updateRelationship =
        UpdateRelationship.ofUpdate(
            resource,
            ADMINISTRATOR,
            subject,
            new Caveat("caveat", Map.of("key", "value", "key2", 123.45, "key3", true)));
    var map = mapper.map(updateRelationship);

    assertEquals(RelationshipUpdate.Operation.OPERATION_TOUCH, map.getOperation());
    assertNotNull(map.getRelationship());
    assertEquals(ADMINISTRATOR, map.getRelationship().getRelation());
    assertEquals(ID, map.getRelationship().getResource().getObjectId());
    assertEquals(TENANT, map.getRelationship().getResource().getObjectType());
    assertNotNull(map.getRelationship().getOptionalCaveat());
  }

  @Test
  void test_mapper_withDeleteOperation() {

    var resource = ObjectRef.of(TENANT, ID);
    var subject = ObjectRef.of(USER, ID);

    var updateRelationship = UpdateRelationship.ofDelete(resource, ADMINISTRATOR, subject);
    var map = mapper.map(updateRelationship);

    assertEquals(RelationshipUpdate.Operation.OPERATION_DELETE, map.getOperation());
    assertNotNull(map.getRelationship());
    assertEquals(ADMINISTRATOR, map.getRelationship().getRelation());
    assertEquals(ID, map.getRelationship().getResource().getObjectId());
    assertEquals(TENANT, map.getRelationship().getResource().getObjectType());
  }
}
