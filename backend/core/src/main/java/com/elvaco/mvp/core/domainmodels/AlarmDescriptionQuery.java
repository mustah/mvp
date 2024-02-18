package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.Builder;

@Builder
public record AlarmDescriptionQuery(
  String manufacturer,
  @Nullable Integer deviceType,
  @Nullable Integer firmwareVersion,
  int mask
) {}
