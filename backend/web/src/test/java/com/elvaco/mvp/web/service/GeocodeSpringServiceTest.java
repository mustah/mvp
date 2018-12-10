package com.elvaco.mvp.web.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class GeocodeSpringServiceTest {

  private HttpClientMock httpClientMock;
  private GeocodeService geocodeService;

  @Before
  public void setUp() {
    httpClientMock = new HttpClientMock();
    geocodeService = new GeocodeSpringService(
      "http://mvp.com",
      "http://geoservice.com:8080",
      httpClientMock
    );
  }

  @Test
  public void shouldNotFetchWhenLocationIsUnknown() {
    LocationWithId location = LocationBuilder.from(UNKNOWN_LOCATION)
      .id(randomUUID())
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationIsMissingAddress() {
    LocationWithId location = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .id(randomUUID())
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationIsMissingCity() {
    LocationWithId location = new LocationBuilder()
      .country("sweden")
      .address("main 1")
      .id(randomUUID())
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationHasCoordinatesAndKnownLocationInfo() {
    LocationWithId location = locationBuilder()
      .id(randomUUID())
      .latitude(1.2)
      .longitude(2.1)
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationHasCoordinatesAndNoLocationInfo() {
    LocationWithId location = new LocationBuilder()
      .id(randomUUID())
      .latitude(1.2)
      .longitude(2.1)
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldTrimUrls() {
    GeocodeService geocodeService = new GeocodeSpringService(
      "http://mvp.com   ",
      "http://geoservice.com:8080  ",
      httpClientMock
    );

    UUID logicalMeterId = randomUUID();

    LocationWithId location = new LocationBuilder()
      .id(logicalMeterId)
      .country("sweden")
      .city("stockholm")
      .address("drottninggatan 1")
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isEqualTo(
      "http://geoservice.com:8080/address?"
        + "country=sweden&city=stockholm&street=drottninggatan 1"
        + "&callbackUrl=http://mvp.com/api/v1/geocodes/callback/" + logicalMeterId
        + "&errorCallbackUrl=http://mvp.com/api/v1/geocodes/error/" + logicalMeterId
        + "&force=false");
  }

  @Test
  public void encodedLocationInformation() {
    UUID logicalMeterId = randomUUID();

    LocationWithId location = locationBuilder()
      .id(logicalMeterId)
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isEqualTo(
      "http://geoservice.com:8080/address"
        + "?country=sweden"
        + "&city=växjö"
        + "&street=drottingvägen 1"
        + "&callbackUrl=http://mvp.com/api/v1/geocodes/callback/" + logicalMeterId
        + "&errorCallbackUrl=http://mvp.com/api/v1/geocodes/error/" + logicalMeterId
        + "&force=false");
  }

  @Test
  public void encodedLocationInformationShouldHaveForceParameter() {
    UUID logicalMeterId = randomUUID();

    LocationWithId location = locationBuilder()
      .id(logicalMeterId)
      .forceUpdate()
      .buildLocationWithId();

    geocodeService.fetchCoordinates(location);

    assertThat(httpClientMock.url).isEqualTo(
      "http://geoservice.com:8080/address?"
        + "country=sweden&city=växjö&street=drottingvägen 1"
        + "&callbackUrl=http://mvp.com/api/v1/geocodes/callback/" + logicalMeterId
        + "&errorCallbackUrl=http://mvp.com/api/v1/geocodes/error/" + logicalMeterId
        + "&force=true");
  }

  private static LocationBuilder locationBuilder() {
    return new LocationBuilder()
      .country("sweden")
      .city("växjö")
      .address("drottingvägen 1");
  }

  private static class HttpClientMock implements Function<String, String> {

    private String url;

    @Override
    public String apply(String url) {
      this.url = URLDecoder.decode(url, StandardCharsets.UTF_8);
      return HttpStatus.OK.name();
    }
  }
}
