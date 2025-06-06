package com.flowers.spicegen.example;

import static org.junit.jupiter.api.Assertions.*;

import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.WriteSchemaRequest;
import com.authzed.grpcutil.BearerToken;
import com.flowers.spicegen.api.*;
import com.flowers.spicegen.permissions.refs.DocumentRef;
import com.flowers.spicegen.permissions.refs.FolderRef;
import com.flowers.spicegen.permissions.refs.TeamRef;
import com.flowers.spicegen.permissions.refs.UserRef;
import com.flowers.spicegen.spicedbbinding.SpiceDbPermissionServiceBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class ExampleTest {

  private static final int GRPC_PORT = 50051;
  private static final String TOKEN = "t0ken";

  private static final Logger logger = LoggerFactory.getLogger(ExampleTest.class);

  @Container
  private static final GenericContainer<?> spicedb =
      new GenericContainer<>(DockerImageName.parse("authzed/spicedb:v1.41.0"))
          .withCommand("serve", "--grpc-preshared-key", TOKEN)
          .waitingFor(Wait.forLogMessage(".*\"grpc server started serving\".*", 1))
          .withLogConsumer(f -> logger.info("spicedb: {}", f.getUtf8String()))
          .withExposedPorts(
              GRPC_PORT, // grpc
              8080, // dashboard
              9090 // metrics
              );

  private PermissionService permissionService;
  private PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionServiceStub;
  private ManagedChannel channel;

  @BeforeEach
  void before() {

    var host = spicedb.getHost();
    var port = spicedb.getMappedPort(GRPC_PORT);

    var bearerToken = new BearerToken(TOKEN);
    channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

    updateSchema();

    // setup the GRPC stub
    permissionServiceStub =
        PermissionsServiceGrpc.newBlockingStub(channel).withCallCredentials(bearerToken);

    // create the permissions service
    permissionService =
        SpiceDbPermissionServiceBuilder.newBuilder()
            .permissionsBlockingStub(permissionServiceStub)
            .build();
  }

  private void updateSchema() {
    var schemaService =
        SchemaServiceGrpc.newBlockingStub(channel).withCallCredentials(new BearerToken(TOKEN));

    schemaService.writeSchema(WriteSchemaRequest.newBuilder().setSchema(loadSchema()).build());
  }

  @Test
  void example() {
    var userId = 7;

    // typesafe object references!
    var user = UserRef.ofLong(userId);
    var seniorUser = UserRef.ofLong(123);
    var minorUser = UserRef.ofLong(321);
    var userWithoutAge = UserRef.ofLong(121);
    var userInTeam = UserRef.ofLong(42);
    var team = TeamRef.ofLong(42);
    var folder = FolderRef.of("home");
    var document = DocumentRef.ofLong(48);

    // EXAMPLE: updating relationships
    var updateResult =
        permissionService.updateRelationships(
            UpdateRelationships.newBuilder()
                // note the generated factory methods!
                .update(folder.createReaderUser(user))
                .update(folder.createAgedReaderUser(seniorUser, 69D))
                .update(folder.createAgedReaderUser(minorUser, 13D))
                .update(folder.createAgedReaderUser(userWithoutAge, null))
                .update(team.createMemberUser(userInTeam))
                .update(folder.createReaderTeamMember(team))
                .update(document.createParentFolderFolder(folder))
                .build());

    var consistencyToken = updateResult.consistencyToken();

    // EXAMPLE: checking permission
    assertTrue(
        permissionService.checkPermission(
            document.checkRead(
                SubjectRef.ofObject(user), Consistency.atLeastAsFreshAs(consistencyToken))));

    assertTrue(
        permissionService.checkPermission(
            document.checkRead(
                SubjectRef.ofObject(seniorUser), Consistency.atLeastAsFreshAs(consistencyToken))));

    assertFalse(
        permissionService.checkPermission(
            document.checkRead(
                SubjectRef.ofObject(minorUser), Consistency.atLeastAsFreshAs(consistencyToken))));

    assertFalse(
        permissionService.checkPermission(
            document.checkRead(
                SubjectRef.ofObject(userWithoutAge),
                Consistency.atLeastAsFreshAs(consistencyToken))));

    assertTrue(
        permissionService.checkPermission(
            document.checkRead(
                SubjectRef.ofObject(userInTeam), Consistency.atLeastAsFreshAs(consistencyToken))));

    // EXAMPLE: checking multiple permissions
    var checkPermissions =
        permissionService.checkBulkPermissions(
            CheckBulkPermissions.newBuilder()
                .item(document.checkBulkRead(SubjectRef.ofObject(user)))
                .item(folder.checkBulkRead(SubjectRef.ofObject(UserRef.of("non-existing"))))
                .consistency(Consistency.atLeastAsFreshAs(consistencyToken))
                .build());

    assertEquals(2, checkPermissions.size());
    assertTrue(checkPermissions.get(0).permissionGranted());
    assertFalse(checkPermissions.get(1).permissionGranted());
  }

  private String loadSchema() {
    try (var is = this.getClass().getResourceAsStream("/files.zed")) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      fail(e);
    }
    return "";
  }
}
