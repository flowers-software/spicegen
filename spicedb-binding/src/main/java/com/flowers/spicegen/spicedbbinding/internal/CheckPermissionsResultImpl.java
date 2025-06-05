package com.flowers.spicegen.spicedbbinding.internal;

import com.flowers.spicegen.api.CheckBulkPermissionItem;
import com.flowers.spicegen.api.CheckBulkPermissionsResult;

public record CheckPermissionsResultImpl(boolean permissionGranted, CheckBulkPermissionItem request)
    implements CheckBulkPermissionsResult {}
