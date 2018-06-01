package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapMarker extends IdentifiableType<UUID> {

  public final UUID id;
  public final StatusType status;
  public final double latitude;
  public final double longitude;

  @Override
  public UUID getId() {
    return id;
  }
}
