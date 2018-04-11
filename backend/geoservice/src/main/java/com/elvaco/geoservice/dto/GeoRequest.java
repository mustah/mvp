package com.elvaco.geoservice.dto;

import java.net.URI;
import java.net.URISyntaxException;

import com.elvaco.geoservice.UriUtils;
import lombok.Getter;

@Getter
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

  public void setCallbackUrl(String encodedCallbackUrl) throws URISyntaxException {
    this.callbackUrl = UriUtils.asDecoded(encodedCallbackUrl);
  }

  public void setErrorCallbackUrl(String encodedErrorCallbackUrl) throws URISyntaxException {
    this.errorCallbackUrl = UriUtils.asDecoded(encodedErrorCallbackUrl);
  }

  public void setStreet(String street) {
    this.street = UriUtils.decode(street);
  }

  public void setCity(String city) {
    this.city = UriUtils.decode(city);
  }

  public void setCountry(String country) {
    this.country = UriUtils.decode(country);
  }
}
