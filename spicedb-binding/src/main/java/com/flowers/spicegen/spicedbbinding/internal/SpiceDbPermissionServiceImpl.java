package com.flowers.spicegen.spicedbbinding.internal;

import com.authzed.api.v1.*;
import com.flowers.spicegen.api.*;
import com.flowers.spicegen.api.PermissionService;
import com.flowers.spicegen.api.exceptions.ClientException;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class SpiceDbPermissionServiceImpl implements PermissionService {

  private final PreconditionMapper preconditionMapper = new PreconditionMapper();

  private final ObjectReferenceMapper objectReferenceMapper = new ObjectReferenceMapper();
  private final SubjectReferenceMapper subjectReferenceMapper = new SubjectReferenceMapper();
  private final ConsistencyMapper consistencyMapper = new ConsistencyMapper();

  private final UpdateRelationshipMapper updateRelationshipMapper =
      new UpdateRelationshipMapper(objectReferenceMapper, subjectReferenceMapper);

  private final CheckPermissionMapper checkPermissionMapper =
      new CheckPermissionMapper(consistencyMapper, objectReferenceMapper, subjectReferenceMapper);

  private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;

  private final GrpcExceptionMapper exceptionMapper = new GrpcExceptionMapper();

  public SpiceDbPermissionServiceImpl(
      PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService) {
    this.permissionsService = permissionsService;
  }

  @Override
  public UpdateResult updateRelationships(UpdateRelationships updates) {

    var mappedUpdates = updates.updates().stream().map(updateRelationshipMapper::map).toList();
    var mappedPreconditions =
        updates.preconditions().stream().map(preconditionMapper::map).toList();

    var req =
        WriteRelationshipsRequest.newBuilder()
            .addAllOptionalPreconditions(mappedPreconditions)
            .addAllUpdates(mappedUpdates)
            .build();

    try {
      var res = permissionsService.writeRelationships(req);
      var zedToken = res.getWrittenAt().getToken();
      return new UpdateResultImpl(zedToken);
    } catch (StatusRuntimeException e) {
      throw exceptionMapper.map(e);
    }
  }

  @Override
  public boolean checkPermission(CheckPermission checkPermission) {

    var request = checkPermissionMapper.map(checkPermission);

    try {
      var response = permissionsService.checkPermission(request);
      return response.getPermissionship()
          == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
    } catch (StatusRuntimeException e) {
      throw exceptionMapper.map(e);
    }
  }

  @Override
  public List<CheckBulkPermissionsResult> checkBulkPermissions(
      CheckBulkPermissions checkBulkPermissions) {
    var request = checkPermissionMapper.mapBulk(checkBulkPermissions);

    try {
      var response = permissionsService.checkBulkPermissions(request);
      if (response.getPairsCount() != checkBulkPermissions.items().size()) {
        throw new ClientException("Amount of response pairs does not match request");
      }
      var results = new ArrayList<CheckBulkPermissionsResult>(response.getPairsCount());
      for (var i = 0; i < response.getPairsList().size(); i++) {
        var checkBulkPermissionsPair = response.getPairs(i);
        var permissionGranted =
            checkBulkPermissionsPair.getItem().getPermissionship()
                == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
        results.add(
            new CheckPermissionsResultImpl(permissionGranted, checkBulkPermissions.items().get(i)));
      }
      return results;
    } catch (StatusRuntimeException e) {
      throw exceptionMapper.map(e);
    }
  }

  @Override
  public Iterator<ObjectRef> lookupSubjects(LookupSubjects lookupSubjects) {

    var requestBuilder =
        LookupSubjectsRequest.newBuilder()
            .setPermission(lookupSubjects.permission())
            .setSubjectObjectType(lookupSubjects.subjectType())
            .setResource(objectReferenceMapper.map(lookupSubjects.resource()));
    if (lookupSubjects.subjectRelation() != null) {
      requestBuilder.setOptionalSubjectRelation(lookupSubjects.subjectRelation());
    }
    var request = requestBuilder.build();

    try {
      var response = permissionsService.lookupSubjects(request);
      return StreamSupport.stream(
              Spliterators.spliteratorUnknownSize(response, Spliterator.ORDERED), false)
          .map(
              lookupSubjectsResponse ->
                  lookupSubjects
                      .subjectFactory()
                      .apply(lookupSubjectsResponse.getSubject().getSubjectObjectId()))
          .iterator();
    } catch (StatusRuntimeException e) {
      throw exceptionMapper.map(e);
    }
  }
}
