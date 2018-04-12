package com.elvaco.geoservice;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@UtilityClass
public class UriUtils {

  public static URI asDecoded(String encodedUrl) throws URISyntaxException {
    try {
      return new URI(requireNonNull(decode(encodedUrl)));
    } catch (URISyntaxException e) {
      log.warn("Unable to create uri from: {}", encodedUrl, e);
      throw e;
    }
  }

  public static String decode(String encodedUrlParameter) {
    try {
      return URLDecoder.decode(encodedUrlParameter, "UTF-8");
    } catch (UnsupportedEncodingException ignore) {
      // cannot happen since we always provide encoding
    }
    return encodedUrlParameter;
  }
}
