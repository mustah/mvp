package com.elvaco.mvp.core.domainmodels;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PhysicalMeter {

  @Nullable
  public final Long id;
  public final Organisation organisation;
  public final String identity;
  public final String medium;
  @Nullable
  public final Long logicalMeterId;
  private List<Measurement> measurements;

  public PhysicalMeter(
    @Nullable Long id,
    Organisation organisation,
    String identity,
    String medium,
    @Nullable Long logicalMeterId
  ) {
    this.id = id;
    this.organisation = organisation;
    this.identity = identity;
    this.medium = medium;
    this.logicalMeterId = logicalMeterId;
    this.measurements = new ArrayList<>();
  }

  public PhysicalMeter(Organisation organisation, String identity, String medium) {
    this(null, organisation, identity, medium, null);
  }

  public PhysicalMeter(
    @Nullable Long id,
    Organisation organisation,
    String identity,
    String medium
  ) {
    this(id, organisation, identity, medium, null);
  }

  public PhysicalMeter(
    Organisation organisation,
    String identity,
    String medium,
    Long logicalMeterId
  ) {
    this(null, organisation, identity, medium, logicalMeterId);
  }
}
