package com.elvaco.geoservice.dto;

import java.net.URI;
import java.net.URISyntaxException;

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
  private URI callbackUrl;
  private URI errorCallbackUrl;
  private String street;
  private String city;
  private String country;

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

  public boolean isValid() {
    return isTrimmedNotEmpty(street)
           && isTrimmedNotEmpty(city)
           && isTrimmedNotEmpty(country)
           && callbackUrl != null
           && errorCallbackUrl != null;
  }

  private static boolean isTrimmedNotEmpty(String str) {
    return str != null && !str.trim().isEmpty();
  }
}
