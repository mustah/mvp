package com.elvaco.mvp.core.domainmodels;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class Asset {

  public final AssetType assetType;
  public final String contentType;
  public final byte[] content;
}
