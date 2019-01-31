package com.elvaco.mvp.core.domainmodels;

import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Medium {
  // hoho
  //FIXME: These should be loaded from the database instead
  UNKNOWN_MEDIUM(1, "Unknown medium"),
  HOT_WATER(2, "Hot water"),
  DISTRICT_HEATING(3, "District heating"),
  DISTRICT_COOLING(4, "District cooling"),
  GAS(5, "Gas"),
  WATER(6, "Water"),
  ROOM_SENSOR(7, "Room sensor"),
  ELECTRICITY(8, "Electricity");

  public final long id;
  public final String name;

  public static Medium from(String mediumName) {
    return Stream.of(values())
      .filter(medium -> medium.name.equalsIgnoreCase(mediumName))
      .findAny()
      .orElse(Medium.UNKNOWN_MEDIUM);
  }
}
