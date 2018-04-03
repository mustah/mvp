package com.elvaco.mvp.web.service;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
public class GeocodeSpringService implements GeocodeService {

  private static final String ERROR_CALLBACK_URL = "/api/v1/geocodes/error/";
  private static final String CALLBACK_URL = "/api/v1/geocodes/callback/";

  private final String mvpUrl;
  private final String geoServiceUrl;
  private final Function<URI, String> httpClient;

  @Async
  @Override
  public void fetchCoordinates(LocationWithId location) {
    if (location.hasNoCoordinates() && location.isKnown()) {
      GeocodeUri.of(geoServiceUrl.trim() + "/byAddress")
        .countryParam(location.getCountry())
        .cityParam(location.getCity())
        .addressParam(location.getAddress())
        .callbackUrl(callbackUrl(location.getId(), CALLBACK_URL))
        .errorCallbackUrl(callbackUrl(location.getId(), ERROR_CALLBACK_URL))
        .toUri()
        .map(httpClient);
    }
  }

  private String callbackUrl(UUID requestId, String callback) {
    return mvpUrl.trim() + callback + requestId;
  }
}
