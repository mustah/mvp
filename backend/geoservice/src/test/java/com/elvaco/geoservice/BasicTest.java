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
  Integer port;

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
  public void test2() {
    Address kabelgatan2t = new Address();
    kabelgatan2t.setStreet("Kabelgatan 2T");
    kabelgatan2t.setCity("Kungsbacka");
    kabelgatan2t.setCountry("Sweden");

    GeoLocation geo = addrService.getGeoByAddress(kabelgatan2t);

    assertEquals(1, geo.getConfidence(), 0.0);
  }

  @Test
  public void testRoundtrip() throws URISyntaxException {
    // Arrange
    AddressDto kabelgatan = new AddressDto();
    kabelgatan.setStreet("Kabelgatan 2T");
    kabelgatan.setCity("Kungsbacka");
    kabelgatan.setCountry("Sweden");
    GeoRequest request = new GeoRequest();
    request.setAddress(kabelgatan);
    request.setCallbackUrl("http://localhost:" + port + "/callback");
    // Act
    geoController.requestByAddress(request);
    try {
      Thread.sleep(2500);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // Assert
    GeoResponse response = (GeoResponse) callbackController.getLastResponse();

    assertEquals(1, response.getGeoData().confidence, 0.0);
    assertEquals(
      "Longitude is wrong",
      Double.valueOf("12.0694219774545"),
      response.getGeoData().longitude
    );
    assertEquals(
      "Latitude is wrong",
      Double.valueOf("57.5052694216628"),
      response.getGeoData().latitude
    );
  }

  @Test
  public void testNotFound() throws URISyntaxException {
    // Arrange
    GeoRequest request = new GeoRequest();
    AddressDto eriksgatan = new AddressDto();
    eriksgatan.setStreet("Eriksgatan 435");
    eriksgatan.setCity("Kungsbacka");
    eriksgatan.setCountry("Sweden");
    request.setAddress(eriksgatan);
    request.setCallbackUrl("http://localhost:" + port + "/callback");
    request.setErrorCallbackUrl("http://localhost:" + port + "/error");
    // Act
    geoController.requestByAddress(request);
    try {
      Thread.sleep(2500);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // Assert
    ErrorDto response = (ErrorDto) callbackController.getLastResponse();
    assertNotNull(response);
    assertEquals("Error message differ", "No geolocation found", response.getMessage());
  }
}
