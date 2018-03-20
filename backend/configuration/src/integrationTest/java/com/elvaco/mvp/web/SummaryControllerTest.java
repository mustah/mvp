package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeterSummaryDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.WAYNE_INDUSTRIES;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SummaryControllerTest extends IntegrationTest {

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private Organisations organisations;

  @Before
  public void setUp() {
    organisations.save(WAYNE_INDUSTRIES);
  }

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
    organisations.deleteById(WAYNE_INDUSTRIES.id);
  }

  @Test
  public void getSummaryOfMetersShouldReturnWithEmptySummaryInfo() {
    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto());
  }

  @Test
  public void getSummaryOfMetersShouldHaveOneResult() {
    logicalMeters.save(newMeter());

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(1, 1, 1));
  }

  @Test
  public void getSummaryOfMetersShouldHaveTwoMetersOneAddress() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeter());

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 1));
  }

  @Test
  public void getSummaryOfMetersShouldHaveTwoMetersTwoAddresses() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .streetAddress("drottninggatan 1")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 2, 2));
  }

  @Test
  public void getSummaryOfMetersShouldHaveThreeMetersTwoCitiesThreeAddresses() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .streetAddress("drottninggatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .streetAddress("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(3, 2, 3));
  }

  @Test
  public void userCanOnlyGetSummaryForMetersWithinTheOrganisation() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .streetAddress("drottninggatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .streetAddress("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asOtherUser()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(0, 0, 0));
  }

  @Test
  public void fetchOnlyMetersInKungsbacka() {
    logicalMeters.save(newMeterWith(sweden()
                                      .city("stockholm")
                                      .streetAddress("kungsgatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .streetAddress("drottninggatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .streetAddress("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters?city=sweden,kungsbacka", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 2));
  }

  private LogicalMeter newMeterWith(LocationBuilder locationBuilder) {
    UUID meterId = randomUUID();
    return new LogicalMeter(
      meterId,
      "externalId-" + meterId,
      context().getOrganisationId(),
      locationBuilder.build(),
      ZonedDateTime.now()
    );
  }

  private LogicalMeter newMeter() {
    return newMeterWith(sweden()
                          .city("stockholm")
                          .streetAddress("kungsgatan 1"));
  }

  private static LocationBuilder sweden() {
    return new LocationBuilder().country("sweden");
  }
}
