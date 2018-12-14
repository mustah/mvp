package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.PeriodBound;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.MeterSummaryDto;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SummaryControllerTest extends IntegrationTest {

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

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
  public void shouldBeAbleToSearchForMetersWithAlarms() {
    var logicalMeter = newLogicalMeter();
    logicalMeters.save(logicalMeter);
    logicalMeters.save(newLogicalMeter());

    var start = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");

    var physicalMeterWithAlarm = physicalMeters.save(
      PhysicalMeter.builder()
        .organisationId(context().organisationId())
        .address("111-222-333-444-1")
        .externalId(randomUUID().toString())
        .medium(Medium.GAS.medium)
        .activePeriod(PeriodRange.from(PeriodBound.inclusiveOf(start)))
        .manufacturer("Elvaco")
        .logicalMeterId(logicalMeter.id)
        .build()
    );

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeterWithAlarm.primaryKey())
      .mask(12)
      .start(start)
      .build());

    var url = Url.builder()
      .path("/summary/meters")
      .parameter(AFTER, start)
      .parameter(BEFORE, start.plusDays(2))
      .parameter(ALARM, "yes")
      .build();

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get(url, MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(1, 1, 1));
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
      .organisationId(context().organisationId())
      .build();

    physicalMeters.save(physicalMeter);

    meterStatusLogs.save(
      StatusLogEntry.builder()
        .primaryKey(physicalMeter.primaryKey())
        .status(StatusType.OK)
        .build()
    );

    meterStatusLogs.save(
      StatusLogEntry.builder()
        .primaryKey(physicalMeter.primaryKey())
        .status(StatusType.WARNING)
        .start(ZonedDateTime.now().plusDays(1))
        .build()
    );

    ResponseEntity<MeterSummaryDto> response = asUser()
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
      .utcOffset(DEFAULT_UTC_OFFSET)
      .build();
  }

  private static LocationBuilder sweden() {
    return new LocationBuilder().country("sweden");
  }
}
