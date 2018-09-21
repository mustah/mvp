package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeterSummaryDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SummaryControllerTest extends IntegrationTest {

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
  }

  @Test
  public void whenNoMetersGetEmptySummaryInfo() {
    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto());
  }

  @Test
  public void getSummaryOfMetersShouldHaveOneResult() {
    logicalMeters.save(newLogicalMeter());

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(1, 1, 1));
  }

  @Test
  public void metersWithSameAddress() {
    logicalMeters.save(newLogicalMeter());
    logicalMeters.save(newLogicalMeter());

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 1));
  }

  @Test
  public void filteredByStatus() {
    logicalMeters.save(newLogicalMeter());
    logicalMeters.save(newLogicalMeterWith(sweden().city("kungsbacka").address("gatan med G")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get(
        "/summary/meters?reported=ok&before=2019-01-01T00:00:00Z&after=2018-01-01T00:00:00Z",
        MeterSummaryDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(0, 0, 0));
  }

  @Test
  public void metersWithDifferentAddresses() {
    logicalMeters.save(newLogicalMeter());
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 1")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 2, 2));
  }

  @Test
  public void metersWithSameCityDifferentStreetAddress() {
    logicalMeters.save(newLogicalMeter());
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 1")));
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(3, 2, 3));
  }

  @Test
  public void metersWithNoLocation() {
    logicalMeters.save(newLogicalMeter());
    logicalMeters.save(newLogicalMeterWithLocation(null));
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(3, 2, 2));
  }

  @Test
  public void userCanOnlyGetSummaryForMetersWithinTheOrganisation() {
    logicalMeters.save(newLogicalMeter());
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 1")));
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asOtherUser()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(0, 0, 0));
  }

  @Test
  public void fetchOnlyMetersInKungsbacka() {
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("stockholm")
      .address("kungsgatan 1")));
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 1")));
    logicalMeters.save(newLogicalMeterWith(sweden()
      .city("kungsbacka")
      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters?city=sweden,kungsbacka", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 2));
  }

  @Test
  public void fetchNumberOfMetersDistinct_WhenTheyHaveManyStatusUpdates() {
    LogicalMeter logicalMeter = newLogicalMeter();

    logicalMeters.save(logicalMeter);
    logicalMeters.save(newLogicalMeter());

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .logicalMeterId(logicalMeter.id)
      .externalId(logicalMeter.externalId)
      .address("v1")
      .manufacturer("ELV")
      .organisation(context().organisation())
      .build();

    physicalMeters.save(physicalMeter);

    meterStatusLogs.save(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.OK)
        .start(ZonedDateTime.now())
        .build()
    );

    meterStatusLogs.save(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.WARNING)
        .start(ZonedDateTime.now().plusDays(1))
        .build()
    );

    ResponseEntity<MeterSummaryDto> response = asTestUser()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 1));
  }

  private LogicalMeter newLogicalMeterWith(LocationBuilder locationBuilder) {
    return newLogicalMeterWithLocation(locationBuilder.build());
  }

  private LogicalMeter newLogicalMeter() {
    return newLogicalMeterWith(sweden()
      .city("stockholm")
      .address("kungsgatan 1"));
  }

  private LogicalMeter newLogicalMeterWithLocation(@Nullable Location location) {
    return LogicalMeter.builder()
      .externalId("externalId-" + randomUUID())
      .organisationId(context().organisationId())
      .location(location)
      .build();
  }

  private static LocationBuilder sweden() {
    return new LocationBuilder().country("sweden");
  }
}
