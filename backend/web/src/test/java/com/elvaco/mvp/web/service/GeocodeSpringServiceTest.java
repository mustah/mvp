package com.elvaco.mvp.web.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.Location;
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
    geocodeService.fetchCoordinates(LocationWithId.of(UNKNOWN_LOCATION, randomUUID()));

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationIsMissingAddress() {
    Location location = new LocationBuilder().country("sweden").city("stockholm").build();

    geocodeService.fetchCoordinates(LocationWithId.of(location, randomUUID()));

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationIsMissingCity() {
    Location location = new LocationBuilder().country("sweden").address("main 1").build();

    geocodeService.fetchCoordinates(LocationWithId.of(location, randomUUID()));

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationHasCoordinatesAndKnownLocationInfo() {
    Location location = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("drottninggatan 1")
      .latitude(1.2)
      .longitude(2.1)
      .build();

    geocodeService.fetchCoordinates(LocationWithId.of(location, randomUUID()));

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldNotFetchWhenLocationHasCoordinatesAndNoLocationInfo() {
    Location location = new LocationBuilder()
      .latitude(1.2)
      .longitude(2.1)
      .build();

    geocodeService.fetchCoordinates(LocationWithId.of(location, randomUUID()));

    assertThat(httpClientMock.url).isNull();
  }

  @Test
  public void shouldTrimUrls() {
    GeocodeService geocodeService = new GeocodeSpringService(
      "http://mvp.com   ",
      "http://geoservice.com:8080  ",
      httpClientMock
    );

    Location location = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("drottninggatan 1")
      .build();

    UUID logicalMeterId = randomUUID();

    geocodeService.fetchCoordinates(LocationWithId.of(location, logicalMeterId));

    assertThat(httpClientMock.url).isEqualTo(
      "http://geoservice.com:8080/address?"
      + "country=sweden&city=stockholm&street=drottninggatan 1"
      + "&callbackUrl=http://mvp.com/api/v1/geocodes/callback/" + logicalMeterId
      + "&errorCallbackUrl=http://mvp.com/api/v1/geocodes/error/" + logicalMeterId);
  }

  @Test
  public void encodedLocationInformation() {
    GeocodeService geocodeService = new GeocodeSpringService(
      "http://mvp.com",
      "http://geoservice.com:8080",
      httpClientMock
    );

    Location location = new LocationBuilder()
      .country("sweden")
      .city("växjö")
      .address("drottingvägen 1")
      .build();

    UUID logicalMeterId = randomUUID();

    geocodeService.fetchCoordinates(LocationWithId.of(location, logicalMeterId));

    assertThat(httpClientMock.url).isEqualTo(
      "http://geoservice.com:8080/address?"
      + "country=sweden&city=växjö&street=drottingvägen 1"
      + "&callbackUrl=http://mvp.com/api/v1/geocodes/callback/" + logicalMeterId
      + "&errorCallbackUrl=http://mvp.com/api/v1/geocodes/error/" + logicalMeterId);
  }

  private static class HttpClientMock implements Function<String, String> {

    private String url;

    @Override
    public String apply(String url) {
      try {
        this.url = URLDecoder.decode(url, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
      return HttpStatus.OK.name();
    }
  }
}
