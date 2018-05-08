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

  public static URI asDecodedUri(String encodedUrl) throws URISyntaxException {
    try {
      return new URI(requireNonNull(decode(encodedUrl)));
    } catch (URISyntaxException e) {
      log.warn("Unable to create uri from: {}", encodedUrl, e);
      throw e;
    }
  }

  public static String decode(String encodedUrlParameter) {
    try {
      return URLDecoder.decode(requireNonNull(trimOrNull(encodedUrlParameter)), "UTF-8");
    } catch (UnsupportedEncodingException ignore) {
      // cannot happen since we always provide encoding
    }
    return encodedUrlParameter;
  }

  private static String trimOrNull(String str) {
    if (str != null) {
      String trimmed = str.trim();
      return trimmed.isEmpty() ? null : trimmed;
    } else {
      return null;
    }
  }
}
