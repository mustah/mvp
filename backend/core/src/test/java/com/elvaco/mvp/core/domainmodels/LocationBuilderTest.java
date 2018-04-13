package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LocationBuilderTest {

  @Test
  public void locationIsSetCorrectly() {
    LocationBuilder builder = new LocationBuilder();
    Location location = builder.confidence(0.5).latitude(45.999).longitude(99.12).build();
    assertThat(location.hasCoordinates()).isTrue();
    GeoCoordinate coordinate = location.getCoordinate();
    assertThat(coordinate.getLatitude()).isEqualTo(45.999);
    assertThat(coordinate.getLongitude()).isEqualTo(99.12);
    assertThat(coordinate.getConfidence()).isEqualTo(0.5);
  }

  @Test
  public void confidenceDefaultsToOneWhenLatLongIsSet() {
    LocationBuilder builder = new LocationBuilder();
    Location location = builder.latitude(45.999).longitude(99.12).build();
    assertThat(location.hasCoordinates()).isTrue();
    GeoCoordinate coordinate = location.getCoordinate();
    assertThat(coordinate.getLatitude()).isEqualTo(45.999);
    assertThat(coordinate.getLongitude()).isEqualTo(99.12);
    assertThat(coordinate.getConfidence()).isEqualTo(1.0);
  }

  @Test
  public void invalidConfidenceThrowsIllegalArgumentException() {
    LocationBuilder builder = new LocationBuilder()
      .latitude(45.999)
      .longitude(99.12)
      .confidence(9999.0);
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(builder::build)
      .withMessageContaining("Confidence should be between 0.0 and 1.0");
  }

  @Test
  public void valuesAreTrimmedToNull() {
    Location location = new LocationBuilder()
      .country("  ")
      .city("   ")
      .address("")
      .build();

    assertThat(location.getCountry()).isNull();
    assertThat(location.getCity()).isNull();
    assertThat(location.getAddress()).isNull();
  }

  @Test
  public void valuesAreTrimmed() {
    Location location = new LocationBuilder()
      .country(" sweden ")
      .city(" kungsbacka  ")
      .address(" kabelgatan 1   ")
      .build();

    assertThat(location.getCountry()).isEqualTo("sweden");
    assertThat(location.getCity()).isEqualTo("kungsbacka");
    assertThat(location.getAddress()).isEqualTo("kabelgatan 1");
  }
}
