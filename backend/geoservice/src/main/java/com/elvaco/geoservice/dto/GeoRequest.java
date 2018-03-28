package com.elvaco.geoservice.dto;

import java.net.URI;
import java.net.URISyntaxException;

public class GeoRequest {
  // CallbackUrl is an url containing requestId and access token to post the reply
  // to. The caller is responsible to provide the details needed to connect the
  // supplied geo-location and address the Gateway/meter to the location
  private URI callbackUrl;
  private URI errorCallbackUrl;
  private AddressDto address;

  public URI getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) throws URISyntaxException {
    this.callbackUrl = new URI(callbackUrl);
  }

  public AddressDto getAddress() {
    return address;
  }

  public void setAddress(AddressDto address) {
    this.address = address;
  }

  public URI getErrorCallbackUrl() {
    return errorCallbackUrl;
  }

  public void setErrorCallbackUrl(String errorCallbackUrl) throws URISyntaxException {
    this.errorCallbackUrl = new URI(errorCallbackUrl);
  }

}
