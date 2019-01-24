package com.elvaco.geoservice.dto;

import java.net.URISyntaxException;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GeoRequestTest {

  private GeoRequest request;
  private Validator validator;

  @Before
  public void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    request = new GeoRequest();
  }

  @Test
  public void isNotValid_WhenNoAddressInfoIsSet() {
    List<String> violations = validate(request);

    assertThat(violations).containsExactlyInAnyOrder(
      "Error callback URL must be provided.",
      "Callback URL must be provided.",
      "Country must be provided.",
      "City must be provided.",
      "Street must be provided.",
      "Zip must be provided but might be blank."
    );
  }

  @Test
  public void isNotValid_WhenStreetIsMissing() {
    request.setCity("kungsbacka");
    request.setCountry("sverige");

    List<String> violations = validate(request);

    assertThat(violations).containsExactlyInAnyOrder(
      "Error callback URL must be provided.",
      "Callback URL must be provided.",
      "Street must be provided.",
      "Zip must be provided but might be blank."
    );
  }

  @Test
  public void isNotValid_WhenCityIsMissing() {
    request.setStreet("street 1");
    request.setCountry("sverige");

    List<String> violations = validate(request);

    assertThat(violations).containsExactlyInAnyOrder(
      "Error callback URL must be provided.",
      "Callback URL must be provided.",
      "City must be provided.",
      "Zip must be provided but might be blank."
    );
  }

  @Test
  public void isNotValid_WhenCountryIsMissing() {
    request.setCity("kungsbacka");
    request.setStreet("street 1");

    List<String> violations = validate(request);

    assertThat(violations).containsExactlyInAnyOrder(
      "Error callback URL must be provided.",
      "Callback URL must be provided.",
      "Country must be provided.",
      "Zip must be provided but might be blank."
    );
  }

  @Test
  public void isNotValid_WhenNoUrlsAreSet() {
    request.setStreet("kabelgatan");
    request.setCity("kungsbacka");
    request.setCountry("sverige");
    request.setZip("43437");

    List<String> violations = validate(request);

    assertThat(violations).containsExactlyInAnyOrder(
      "Error callback URL must be provided.",
      "Callback URL must be provided."
    );
  }

  @Test
  public void isValid_WhenAddressInfoIsAvailable() throws URISyntaxException {
    request.setStreet("kabelgatan");
    request.setZip("43437");
    request.setCity("kungsbacka");
    request.setCountry("sverige");
    request.setCallbackUrl("/callback");
    request.setErrorCallbackUrl("/error");

    List<String> violations = validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  public void isNotValid_WhenCityIsBlack() {
    assertThatThrownBy(() -> request.setCity(" "))
      .isInstanceOf(NullPointerException.class);
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

  private List<String> validate(GeoRequest request) {
    return validator.validate(request).stream()
      .map(ConstraintViolation::getMessage)
      .collect(toList());
  }
}
