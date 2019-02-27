package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class Medium {
  public static final String UNKNOWN_MEDIUM = "Unknown medium";
  public static final String HOT_WATER = "Hot water";
  public static final String DISTRICT_HEATING = "District heating";
  public static final String DISTRICT_COOLING = "District cooling";
  public static final String GAS = "Gas";
  public static final String WATER = "Water";
  public static final String ROOM_SENSOR = "Room sensor";
  public static final String ELECTRICITY = "Electricity";

  public static final List<String> MEDIA = List.of(
    UNKNOWN_MEDIUM,
    HOT_WATER,
    DISTRICT_HEATING,
    DISTRICT_COOLING,
    GAS,
    WATER,
    ROOM_SENSOR,
    ELECTRICITY
  );

  @Nullable
  public final Long id;
  public final String name;
}
