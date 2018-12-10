package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LocationTestData {

  public static LocationBuilder kungsbacka() {
    return new LocationBuilder()
      .country("sverige")
      .city("kungsbacka")
      .address("kabelgatan 1")
      .longitude(11.123)
      .latitude(12.345)
      .confidence(1.0);
  }

  public static LocationBuilder stockholm() {
    return new LocationBuilder()
      .country("sverige")
      .city("stockholm")
      .address("drottninggatan 1337")
      .longitude(1.2345)
      .latitude(6.78910)
      .confidence(1.0);
  }

  public static LocationBuilder oslo() {
    return new LocationBuilder()
      .country("norge")
      .city("olso")
      .address("stigen 1")
      .longitude(1.2345)
      .latitude(6.78910)
      .confidence(1.0);
  }

  public static LocationBuilder locationWithoutCoordinates() {
    return new LocationBuilder()
      .country("sverige")
      .city("kungsbacka")
      .address("teknikgatan 17");
  }
}
