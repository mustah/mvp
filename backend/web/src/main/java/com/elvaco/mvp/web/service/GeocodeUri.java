package com.elvaco.mvp.web.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
class GeocodeUri {

  private static final String UTF_8 = "UTF-8";

  private final UriComponentsBuilder delegate;

  private String country;
  private String city;
  private String address;
  private String callbackUrl;
  private String errorCallbackUrl;

  private GeocodeUri(UriComponentsBuilder delegate) {
    this.delegate = delegate;
  }

  static GeocodeUri of(String httpUrl) {
    return new GeocodeUri(UriComponentsBuilder.fromHttpUrl(httpUrl));
  }

  GeocodeUri countryParam(String country) {
    this.country = country;
    return this;
  }

  GeocodeUri cityParam(String city) {
    this.city = city;
    return this;
  }

  GeocodeUri addressParam(String address) {
    this.address = address;
    return this;
  }

  GeocodeUri callbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
    return this;
  }

  GeocodeUri errorCallbackUrl(String errorCallbackUrl) {
    this.errorCallbackUrl = errorCallbackUrl;
    return this;
  }

  Optional<URI> toUri() {
    try {
      return Optional.of(
        delegate
          .queryParam("address.country", encode(country))
          .queryParam("address.city", encode(city))
          .queryParam("address.street", encode(address))
          .queryParam("callbackUrl", callbackUrl)
          .queryParam("errorCallbackUrl", errorCallbackUrl)
          .build()
          .toUri()
      );
    } catch (UnsupportedEncodingException e) {
      log.warn("Unable to create url: ", e);
    }
    return Optional.empty();
  }

  private static String encode(String s) throws UnsupportedEncodingException {
    return URLEncoder.encode(s, UTF_8);
  }
}
