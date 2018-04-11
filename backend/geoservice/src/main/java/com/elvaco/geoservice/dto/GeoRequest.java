package com.elvaco.geoservice.dto;

import java.net.URI;
import java.net.URISyntaxException;

import com.elvaco.geoservice.UriUtils;

public class GeoRequest {

  /**
   * CallbackUrl is an url containing requestId and access token to post the reply to. The caller
   * is responsible to provide the details needed to connect the supplied geo-location and address
   * the gateway/meter to the location.
   */
  private URI callbackUrl;
  private URI errorCallbackUrl;
  private String street;
  private String city;
  private String country;

  public URI getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String encodedCallbackUrl) throws URISyntaxException {
    this.callbackUrl = UriUtils.asDecoded(encodedCallbackUrl);
  }

  public URI getErrorCallbackUrl() {
    return errorCallbackUrl;
  }

  public void setErrorCallbackUrl(String encodedErrorCallbackUrl) throws URISyntaxException {
    this.errorCallbackUrl = UriUtils.asDecoded(encodedErrorCallbackUrl);
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = UriUtils.decode(street);
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = UriUtils.decode(city);
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = UriUtils.decode(country);
  }
}
