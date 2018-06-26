package com.elvaco.mvp.web.service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.Location;
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
  private final Function<String, String> httpClient;

  @Async
  @Override
  public void fetchCoordinates(LocationWithId location) {
    Optional.of(location)
      .filter(Location::isKnown)
      .filter(Location::hasNoCoordinates)
      .flatMap(this::makeGeoServiceUrl)
      .map(httpClient);
  }

  private Optional<String> makeGeoServiceUrl(LocationWithId meterLocation) {
    return GeocodeUri.of(geoServiceUrl.trim() + "/address")
      .countryParam(meterLocation.getCountry())
      .cityParam(meterLocation.getCity())
      .addressParam(meterLocation.getAddress())
      .forceUpdateParam(meterLocation.shouldForceUpdate)
      .callbackUrl(callbackUrl(meterLocation.getId(), CALLBACK_URL))
      .errorCallbackUrl(callbackUrl(meterLocation.getId(), ERROR_CALLBACK_URL))
      .toUriString();
  }

  private String callbackUrl(UUID requestId, String callback) {
    return mvpUrl.trim() + callback + requestId;
  }
}
