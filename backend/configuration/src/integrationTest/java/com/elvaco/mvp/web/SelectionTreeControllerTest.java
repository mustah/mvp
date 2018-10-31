package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.SelectionTreeDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.oslo;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionTreeControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void getResponseOk() {
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId1")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId2")
      .organisationId(context().organisationId())
      .location(stockholm().build())
      .build());

    ResponseEntity<SelectionTreeDto> response = asSuperAdmin()
      .get("/selection-tree", SelectionTreeDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void getFilteredCity() {
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId1")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId2")
      .organisationId(context().organisationId())
      .location(kungsbacka().address("kabelgatan 2").build())
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId3")
      .organisationId(context().organisationId())
      .location(stockholm().build())
      .build());

    var response = asSuperAdmin()
      .get("/selection-tree?city=sverige,kungsbacka", SelectionTreeDto.class)
      .getBody();

    var cities = response.cities;
    assertThat(cities).hasSize(1);
    assertThat(cities.get(0).addresses).extracting("name")
      .containsExactlyInAnyOrder("kabelgatan 1", "kabelgatan 2");
  }

  @Test
  public void getFilteredAddress() {
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId1")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId2")
      .organisationId(context().organisationId())
      .location(kungsbacka().address("kabelgatan 2").build())
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId3")
      .organisationId(context().organisationId())
      .location(kungsbacka().address("kabelgatan 3").build())
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .externalId("extId4")
      .organisationId(context().organisationId())
      .location(oslo().address("kabelgatan 2").build())
      .build());

    var response = asSuperAdmin()
      .get("/selection-tree?address=sverige,kungsbacka,kabelgatan+2", SelectionTreeDto.class)
      .getBody();

    assertThat(response.cities).hasSize(1);
    assertThat(response.cities.get(0).addresses).extracting("name").containsExactly("kabelgatan 2");
  }
}
