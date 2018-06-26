package com.elvaco.geoservice.dto;

import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.ToString;

import static com.elvaco.geoservice.UriUtils.asDecodedUri;
import static com.elvaco.geoservice.UriUtils.decode;

@ToString
@Getter
public class GeoRequest {

  /**
   * CallbackUrl is an url containing requestId and access token to post the reply to. The caller
   * is responsible to provide the details needed to connect the supplied geo-location and address
   * the gateway/meter to the location.
   */
  @NotNull(message = "Callback URL must be provided.")
  private URI callbackUrl;

  @NotNull(message = "Error callback URL must be provided.")
  private URI errorCallbackUrl;

  @NotBlank(message = "Street must be provided.")
  private String street;

  @NotBlank(message = "City must be provided.")
  private String city;

  @NotBlank(message = "Country must be provided.")
  private String country;

  private boolean force;

  public void setCallbackUrl(String encodedCallbackUrl) throws URISyntaxException {
    this.callbackUrl = asDecodedUri(encodedCallbackUrl);
  }

  public void setErrorCallbackUrl(String encodedErrorCallbackUrl) throws URISyntaxException {
    this.errorCallbackUrl = asDecodedUri(encodedErrorCallbackUrl);
  }

  public void setStreet(String street) {
    this.street = decode(street);
  }

  public void setCity(String city) {
    this.city = decode(city);
  }

  public void setCountry(String country) {
    this.country = decode(country);
  }

  public void setForce(boolean force) {
    this.force = force;
  }
}
