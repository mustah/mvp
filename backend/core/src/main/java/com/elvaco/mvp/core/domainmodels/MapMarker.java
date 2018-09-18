package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;
import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapMarker extends IdentifiableType<UUID> {

  public final UUID id;
  public final StatusType status;
  @Nullable
  public final Integer alarm;
  public final double latitude;
  public final double longitude;

  @Override
  public UUID getId() {
    return id;
  }
}
