package com.elvaco.geoservice;

import java.net.URISyntaxException;

import com.elvaco.geoservice.controller.GeoController;
import com.elvaco.geoservice.dto.AddressDto;
import com.elvaco.geoservice.dto.ErrorDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    geocodeFarmService.setUrl("http://localhost:" + port + "/v3/json/forward/?addr={address}");
    callbackController.setLastResponse(null);
  }

  @Test
  public void fetchByFullAddressInfo() {
    Address address = new Address("Kabelgatan 2T", "Kungsbacka", "Sweden");

    GeoLocation geo = addrService.getGeoByAddress(address);

    assertEquals(1, geo.getConfidence(), 0.0);
  }

  @Test
  public void fetchAndRespondToCallbackUrl() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setAddress(new AddressDto("Kabelgatan 2T", "Kungsbacka", "Sweden"));
    request.setCallbackUrl("http://localhost:" + port + "/callback");

    geoController.requestByAddress(request);

    sleep();

    GeoResponse response = (GeoResponse) callbackController.getLastResponse();
    assertEquals(1, response.geoData.confidence, 0.0);
    assertEquals(
      "Longitude is wrong",
      Double.valueOf("12.0694219774545"),
      response.geoData.longitude
    );
    assertEquals(
      "Latitude is wrong",
      Double.valueOf("57.5052694216628"),
      response.geoData.latitude
    );
  }

  @Test
  public void notFound() throws URISyntaxException {
    GeoRequest request = new GeoRequest();
    request.setAddress(new AddressDto("Eriksgatan 435", "Kungsbacka", "Sweden"));
    request.setCallbackUrl("http://localhost:" + port + "/callback");
    request.setErrorCallbackUrl("http://localhost:" + port + "/error");

    geoController.requestByAddress(request);

    sleep();
    ErrorDto response = (ErrorDto) callbackController.getLastResponse();
    assertNotNull(response);
    assertEquals("Error message differ", "No geolocation found", response.message);
  }

  private static void sleep() {
    try {
      Thread.sleep(2500);
    } catch (InterruptedException ignore) {}
  }
}
