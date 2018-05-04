package com.elvaco.geoservice;

import java.net.URISyntaxException;

import com.elvaco.geoservice.controller.GeoController;
import com.elvaco.geoservice.dto.AddressDto;
import com.elvaco.geoservice.dto.ErrorDto;
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
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
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

  @Before
  public void before() {
    geocodeFarmService.setUrl(urlOf("/v3/json/forward/?addr={address}"));
    callbackController.setLastResponse(null);
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
    request.setCallbackUrl(getEncodedCallbackUrl());
    request.setErrorCallbackUrl(getEncodedCallbackUrl());

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
  public void fetchAndRespondToEncodedCallbackUrl() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setStreet("Drottningvägen 1");
    request.setCity("Växjö");
    request.setCountry("Sweden");
    request.setCallbackUrl(getEncodedCallbackUrl());
    request.setErrorCallbackUrl("/testing");

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
  public void notFound() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setStreet("Eriksgatan 435");
    request.setCity("Kungsbacka");
    request.setCountry("Sweden");
    request.setCallbackUrl(getEncodedCallbackUrl());
    request.setErrorCallbackUrl(urlOf("/error"));

    geoController.requestByAddress(request);

    sleep();

    ErrorDto response = (ErrorDto) callbackController.getLastResponse();

    assertThat(response.message)
      .as("Error message differ")
      .isEqualTo("No geolocation found");
  }

  @Test
  public void requestByAddress_WithIncompleteAddressParameters() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setCountry("Sverige");
    request.setCallbackUrl(getEncodedCallbackUrl());
    request.setErrorCallbackUrl(urlOf("/error"));

    ResponseEntity<String> response = geoController.requestByAddress(request);
    String status = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(status).isNull();
  }

  private String getEncodedCallbackUrl() {
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
