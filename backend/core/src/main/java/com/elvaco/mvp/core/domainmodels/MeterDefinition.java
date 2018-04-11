package com.elvaco.mvp.core.domainmodels;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.domainmodels.Medium.DISTRICT_COOLING;
import static com.elvaco.mvp.core.domainmodels.Medium.DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.Medium.HOT_WATER;
import static com.elvaco.mvp.core.domainmodels.Medium.UNKNOWN_MEDIUM;
import static java.util.Collections.unmodifiableSet;

@EqualsAndHashCode
@ToString
public class MeterDefinition implements Identifiable<MeterDefinitionType> {

  public static final MeterDefinition UNKNOWN_METER =
    MeterDefinition.fromMedium(Medium.from(UNKNOWN_MEDIUM.medium));

  public static final MeterDefinition HOT_WATER_METER =
    MeterDefinition.fromMedium(Medium.from(HOT_WATER.medium));

  public static final MeterDefinition DISTRICT_COOLING_METER =
    MeterDefinition.fromMedium(Medium.from(DISTRICT_COOLING.medium));

  public static final MeterDefinition DISTRICT_HEATING_METER =
    MeterDefinition.fromMedium(Medium.from(DISTRICT_HEATING.medium));

  public final MeterDefinitionType type;
  public final String medium;
  public final Set<Quantity> quantities;
  public final boolean systemOwned;

  public MeterDefinition(
    MeterDefinitionType type,
    String medium,
    Set<Quantity> quantities,
    boolean systemOwned
  ) {
    this.type = type;
    this.medium = medium;
    this.quantities = unmodifiableSet(quantities);
    this.systemOwned = systemOwned;
  }

  public static MeterDefinition fromMedium(Medium medium) {
    return MeterDefinition.systemOwned(
      medium.meterDefinitionType,
      medium.medium,
      medium.quantities()
    );
  }

  public static MeterDefinition systemOwned(
    MeterDefinitionType type,
    String medium,
    Set<Quantity> quantities
  ) {
    return new MeterDefinition(type, medium, quantities, true);
  }

  @Override
  public MeterDefinitionType getId() {
    return type;
  }
}
