package com.flowers.spicegen.spicedbbinding.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.authzed.api.v1.*;
import com.flowers.spicegen.api.*;
import com.flowers.spicegen.api.Consistency;
import org.junit.jupiter.api.Test;

class SpiceDbPermissionServiceImplTest {

  @Test
  void updateRelationships() {

    var stub = mock(PermissionsServiceGrpc.PermissionsServiceBlockingStub.class);
    var sut = new SpiceDbPermissionServiceImpl(stub);

    var o = ObjectRef.of("file", "/test.txt");
    var s = ObjectRef.of("user", "bob");
    var updates =
        UpdateRelationships.newBuilder()
            .update(UpdateRelationship.ofUpdate(o, "owner", s, null))
            .build();

    var token = "atXyz";
    var res =
        WriteRelationshipsResponse.newBuilder()
            .setWrittenAt(ZedToken.newBuilder().setToken(token).build())
            .build();
    when(stub.writeRelationships(any())).thenReturn(res);

    // when
    var got = sut.updateRelationships(updates);

    // then
    assertEquals(token, got.consistencyToken());
  }

  @Test
  void checkPermission() {

    var stub = mock(PermissionsServiceGrpc.PermissionsServiceBlockingStub.class);
    var sut = new SpiceDbPermissionServiceImpl(stub);

    var permission = "write";
    var consistency = Consistency.fullyConsistent();

    var o = ObjectRef.of("file", "/test.txt");
    var s = SubjectRef.ofObject(ObjectRef.of("user", "bob"));

    var res =
        CheckPermissionResponse.newBuilder()
            .setPermissionship(CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION)
            .build();
    when(stub.checkPermission(any())).thenReturn(res);

    // when
    var got =
        sut.checkPermission(
            CheckPermission.newBuilder()
                .permission(permission)
                .consistency(consistency)
                .resource(o)
                .subject(s)
                .build());

    // then
    assertTrue(got);
  }

  @Test
  void checkBulkPermissions() {

    var stub = mock(PermissionsServiceGrpc.PermissionsServiceBlockingStub.class);
    var sut = new SpiceDbPermissionServiceImpl(stub);

    var permission1 = "read";
    var permission2 = "write";

    var o = ObjectRef.of("file", "/test.txt");
    var s = SubjectRef.ofObject(ObjectRef.of("user", "bob"));

    var res =
        CheckBulkPermissionsResponse.newBuilder()
            .addPairs(
                CheckBulkPermissionsPair.newBuilder()
                    .setItem(
                        CheckBulkPermissionsResponseItem.newBuilder()
                            .setPermissionship(
                                CheckPermissionResponse.Permissionship
                                    .PERMISSIONSHIP_HAS_PERMISSION)))
            .addPairs(
                CheckBulkPermissionsPair.newBuilder()
                    .setItem(
                        CheckBulkPermissionsResponseItem.newBuilder()
                            .setPermissionship(
                                CheckPermissionResponse.Permissionship
                                    .PERMISSIONSHIP_NO_PERMISSION)))
            .build();
    when(stub.checkBulkPermissions(any())).thenReturn(res);

    // when
    var got =
        sut.checkBulkPermissions(
            CheckBulkPermissions.newBuilder()
                .item(
                    CheckBulkPermissionItem.newBuilder()
                        .permission(permission1)
                        .resource(o)
                        .subject(s)
                        .build())
                .item(
                    CheckBulkPermissionItem.newBuilder()
                        .permission(permission2)
                        .resource(o)
                        .subject(s)
                        .build())
                .build());

    // then
    assertEquals(2, got.size());
    assertTrue(got.get(0).permissionGranted());
    assertFalse(got.get(1).permissionGranted());
  }
}
