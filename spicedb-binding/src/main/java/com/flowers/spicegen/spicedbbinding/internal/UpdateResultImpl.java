package com.flowers.spicegen.spicedbbinding.internal;

import com.flowers.spicegen.api.UpdateResult;

public record UpdateResultImpl(String consistencyToken) implements UpdateResult {}
