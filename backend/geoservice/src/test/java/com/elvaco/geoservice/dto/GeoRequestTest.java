package com.elvaco.geoservice.dto;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GeoRequestTest {

  private GeoRequest request;

  @Before
  public void setUp() {
    request = new GeoRequest();
  }

  @Test
  public void isNotValid_WhenNoAddressInfoIsSet() {
    assertThat(request.isValid()).isFalse();
  }

  @Test
  public void isNotValid_WhenStreetIsMissing() {
    request.setCity("kungsbacka");
    request.setCountry("sverige");

    assertThat(request.isValid()).isFalse();
  }

  @Test
  public void isNotValid_WhenCityIsMissing() {
    request.setStreet("street 1");
    request.setCountry("sverige");

    assertThat(request.isValid()).isFalse();
  }

  @Test
  public void isNotValid_WhenCountryIsMissing() {
    request.setCity("kungsbacka");
    request.setStreet("street 1");

    assertThat(request.isValid()).isFalse();
  }

  @Test
  public void isNotValid_WhenNoUrlsAreSet() {
    request.setStreet("kabelgatan");
    request.setCity("kungsbacka");
    request.setCountry("sverige");

    assertThat(request.isValid()).isFalse();
  }

  @Test
  public void isValid_WhenAddressInfoIsAvailable() throws URISyntaxException {
    request.setStreet("kabelgatan");
    request.setCity("kungsbacka");
    request.setCountry("sverige");
    request.setCallbackUrl("/callback");
    request.setErrorCallbackUrl("/error");

    assertThat(request.isValid()).isTrue();
  }

  @Test
  public void isNotValid_WhenCallbackUrlIsEmpty() {
    assertThatThrownBy(() -> request.setCallbackUrl(""))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void isNotValid_WhenErrorCallbackUrlIsEmpty() {
    assertThatThrownBy(() -> request.setErrorCallbackUrl(""))
      .isInstanceOf(NullPointerException.class);
  }
}
