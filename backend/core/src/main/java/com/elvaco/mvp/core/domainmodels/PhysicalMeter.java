package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

public class PhysicalMeter {
  @Nullable
  public final Long id;
  public final Organisation organisation;
  public final String identity;
  public final String medium;

  public PhysicalMeter(
    @Nullable Long id,
    Organisation organisation,
    String identity,
    String medium
  ) {
    this.id = id;
    this.organisation = organisation;
    this.identity = identity;
    this.medium = medium;
  }
}
