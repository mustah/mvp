package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapMarker {

  public final UUID id;
  public final StatusType status;
  public final double latitude;
  public final double longitude;
}
