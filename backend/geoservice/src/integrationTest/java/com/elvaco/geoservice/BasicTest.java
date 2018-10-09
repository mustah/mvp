package com.elvaco.geoservice;

import java.net.URISyntaxException;

import com.elvaco.geoservice.controller.GeoController;
import com.elvaco.geoservice.dto.AddressDto;
import com.elvaco.geoservice.dto.ErrorDto;
import com.elvaco.geoservice.dto.FieldErrorsDto;
import com.elvaco.geoservice.dto.GeoDataDto;
import com.elvaco.geoservice.dto.GeoRequest;
import com.elvaco.geoservice.dto.GeoResponse;
import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.GeoLocation;
import com.elvaco.geoservice.service.AddressToGeoService;
import com.elvaco.geoservice.service.geocodefarm.GeocodeFarmService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BasicTest {

  @LocalServerPort
  private Integer port;

  @Autowired
  private AddressToGeoService addrService;

  @Autowired
  private GeoController geoController;

  @Autowired
  private GeocodeFarmService geocodeFarmService;

  @Autowired
  private CallbackTestController callbackController;

  @Autowired MockedGeoService mockedGeoService;

  @Before
  public void before() {
    geocodeFarmService.setUrl(urlOf("/v3/json/forward/?addr={address}&country={country}"));
    callbackController.setLastResponse(null);
    mockedGeoService.clearCount();
  }

  @Test
  public void fetchByFullAddressInfo() {
    Address address = new Address("Kabelgatan 2T", "Kungsbacka", "Sweden");

    GeoLocation geo = addrService.getGeoByAddress(address);

    assertThat(geo.getConfidence()).isEqualTo(1.0);
  }

  @Test
  public void fetchAndRespondToCallbackUrl() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setStreet("Kabelgatan 2T");
    request.setCity("Kungsbacka");
    request.setCountry("Sweden");
    request.setCallbackUrl(callbackUrl());
    request.setErrorCallbackUrl(callbackUrl());

    geoController.requestByAddress(request);

    sleep();

    GeoResponse response = (GeoResponse) callbackController.getLastResponse();

    assertThat(response.geoData).isEqualTo(new GeoDataDto(
      Double.valueOf("12.0694219774545"),
      Double.valueOf("57.5052694216628"),
      1.0
    ));

    assertThat(response.address).isEqualTo(new AddressDto("Kabelgatan 2T", "Kungsbacka", "Sweden"));
  }

  @Test
  public void fetchTwoWithForce() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setStreet("Kabelgatan 2T");
    request.setCity("Kungsbacka");
    request.setCountry("Sweden");
    request.setCallbackUrl(callbackUrl());
    request.setErrorCallbackUrl(callbackUrl());

    geoController.requestByAddress(request);

    sleep();

    GeoResponse response = (GeoResponse) callbackController.getLastResponse();

    assertThat(response.geoData).isEqualTo(new GeoDataDto(
      Double.valueOf("12.0694219774545"),
      Double.valueOf("57.5052694216628"),
      1.0
    ));
    request.setForce(true);
    geoController.requestByAddress(request);
    sleep();
    assertThat(mockedGeoService.getRequestCount()).isEqualTo(1);
  }

  @Test
  public void fetchTwoWithoutForce() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setStreet("Kabelgatan 2T");
    request.setCity("Kungsbacka");
    request.setCountry("Sweden");
    request.setCallbackUrl(callbackUrl());
    request.setErrorCallbackUrl(callbackUrl());

    geoController.requestByAddress(request);

    sleep();

    GeoResponse response = (GeoResponse) callbackController.getLastResponse();

    assertThat(response.geoData).isEqualTo(new GeoDataDto(
      Double.valueOf("12.0694219774545"),
      Double.valueOf("57.5052694216628"),
      1.0
    ));
    request.setForce(false);
    geoController.requestByAddress(request);
    sleep();
    assertThat(mockedGeoService.getRequestCount()).isEqualTo(1);
  }

  @Test
  public void fetchAndRespondToEncodedCallbackUrl() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setStreet("Drottningvägen 1");
    request.setCity("Växjö");
    request.setCountry("Sweden");
    request.setCallbackUrl(callbackUrl());
    request.setErrorCallbackUrl(errorCallbackUrl());

    ResponseEntity<String> response = geoController.requestByAddress(request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("OK");

    sleep();

    GeoResponse lastResponse = (GeoResponse) callbackController.getLastResponse();

    assertThat(lastResponse.geoData).isEqualTo(new GeoDataDto(
      Double.valueOf("14.8917824398705"),
      Double.valueOf("57.0294993437130"),
      0.5
    ));

    assertThat(lastResponse.address).isEqualTo(new AddressDto(
      "Drottningvägen 1",
      "Växjö",
      "Sweden"
    ));
  }

  @Test
  public void notFound() {
    ResponseEntity<String> response = new TestRestTemplate()
      .getForEntity(
        urlOf("/address?city=kungsbacka&country=sverige&street=Eriksgatan 435&callbackUrl="
              + callbackUrl()
              + "&errorCallbackUrl=" + errorCallbackUrl()),
        String.class
      );

    sleep();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    ErrorDto lastResponse = (ErrorDto) callbackController.getLastResponse();

    assertThat(lastResponse.message)
      .as("Error message differ")
      .isEqualTo("No geolocation found");
  }

  private String errorCallbackUrl() {
    return urlOf("/error");
  }

  @Test
  public void requestByAddress_WithIncompleteAddressParameters() {
    ResponseEntity<FieldErrorsDto> response = new TestRestTemplate()
      .getForEntity(
        urlOf("/address?city=sverige"),
        FieldErrorsDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().fieldErrors).containsExactlyInAnyOrder(
      "Callback URL must be provided.",
      "Street must be provided.",
      "Error callback URL must be provided.",
      "Country must be provided."
    );
  }

  private String callbackUrl() {
    return urlOf("/callback");
  }

  private String urlOf(String path) {
    return "http://localhost:" + port + path;
  }

  private static void sleep() {
    try {
      Thread.sleep(2500);
    } catch (InterruptedException ignore) {
    }
  }
}
