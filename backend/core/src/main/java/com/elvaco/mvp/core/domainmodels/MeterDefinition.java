package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.domainmodels.Medium.COLD_WATER;
import static com.elvaco.mvp.core.domainmodels.Medium.DISTRICT_COOLING;
import static com.elvaco.mvp.core.domainmodels.Medium.DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.Medium.ELECTRICITY;
import static com.elvaco.mvp.core.domainmodels.Medium.GAS;
import static com.elvaco.mvp.core.domainmodels.Medium.HOT_WATER;
import static com.elvaco.mvp.core.domainmodels.Medium.ROOM_TEMP;
import static com.elvaco.mvp.core.domainmodels.Medium.UNKNOWN_MEDIUM;
import static com.elvaco.mvp.core.domainmodels.Medium.WATER;
import static java.util.Collections.unmodifiableSet;

@EqualsAndHashCode
@ToString
public class MeterDefinition implements Identifiable<MeterDefinitionType>, Serializable {

  public static final MeterDefinition UNKNOWN_METER = fromMedium(UNKNOWN_MEDIUM);

  public static final MeterDefinition HOT_WATER_METER = fromMedium(HOT_WATER);

  public static final MeterDefinition DISTRICT_COOLING_METER = fromMedium(DISTRICT_COOLING);

  public static final MeterDefinition DISTRICT_HEATING_METER = fromMedium(DISTRICT_HEATING);

  public static final MeterDefinition GAS_METER = fromMedium(GAS);

  public static final MeterDefinition WATER_METER = fromMedium(WATER);

  public static final MeterDefinition COLD_WATER_METER = fromMedium(COLD_WATER);

  public static final MeterDefinition ROOM_TEMP_METER = fromMedium(ROOM_TEMP);

  public static final MeterDefinition ELECTRICITY_METER = fromMedium(ELECTRICITY);

  private static final long serialVersionUID = -1017521226747299812L;

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
