package com.elvaco.mvp.web;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterStatus;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.fixture.DomainModels;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatuses;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterStatusJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.util.Dates;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class LogicalMeterControllerTest extends IntegrationTest {

  private final Date statusLogDate = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

  PhysicalMeter meter1;
  PhysicalMeter meter2;
  PhysicalMeter meter3;
  PhysicalMeter meter4;

  MeterDefinition districtHeatingMeterDefinition;
  MeterDefinition hotWaterMeterDefinition;
  @Autowired
  private LogicalMeters logicalMeterRepository;
  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;
  @Autowired
  private MeasurementJpaRepository measurementJpaRepository;
  @Autowired
  private MeasurementUseCases measurementUseCases;
  @Autowired
  private MeterDefinitions meterDefinitions;
  @Autowired
  private PhysicalMeters physicalMeters;
  @Autowired
  private MeterStatuses meterStatuses;
  @Autowired
  private MeterStatusLogs meterStatusLogs;
  @Autowired
  private MeterStatusJpaRepository meterStatusJpaRepository;
  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Before
  public void setUp() {
    districtHeatingMeterDefinition = meterDefinitions.save(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    hotWaterMeterDefinition = meterDefinitions.save(
      MeterDefinition.HOT_WATER_METER
    );

    for (int seed = 1; seed <= 55; seed++) {
      MeterDefinition meterDefinition = seed % 10 == 0 ? hotWaterMeterDefinition :
        districtHeatingMeterDefinition;
      saveLogicalMeter(seed, meterDefinition);
    }

    createAndConnectPhysicalMeters(logicalMeterRepository.findAll());

    createStatusMockData();

    List<PhysicalMeter> meters = physicalMeters.findAll();
    meter1 = meters.get(0);
    meter2 = meters.get(1);
    meter3 = meters.get(2);
    meter4 = meters.get(3);

    MeterStatus meterStatus = meterStatuses.findAll().get(0);
    prepareMeterLogsForStatusPeriodTest(meter1, meter2, meter3, meter4, meterStatus);

  }

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
    meterStatusJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterRepository.deleteAll();
  }

  @Test
  public void findById() {
    ResponseEntity<LogicalMeterDto> response = asElvacoUser()
      .get("/meters/" + meter1.logicalMeterId, LogicalMeterDto.class);

    LogicalMeterDto logicalMeterDto = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(logicalMeterDto.id)
      .as("Unexpected meter id")
      .isEqualTo(meter1.logicalMeterId);

    assertThat(logicalMeterDto.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(1);

    assertThat(logicalMeterDto.statusChangelog.get(0).start)
      .as("Unexpected date format")
      .isEqualTo(Dates.formatTime(statusLogDate, TimeZone.getDefault()));

    assertThat(logicalMeterDto.statusChangelog.get(0).stop)
      .as("Unexpected date format")
      .isEqualTo(Dates.formatTime(statusLogDate, TimeZone.getDefault()));
  }

  @Test
  public void findAllPaged() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage("/meters", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(20);
    assertThat(response.getTotalPages()).isEqualTo(3);

    response = asElvacoUser()
      .getPage("/meters?page=2", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(15);
    assertThat(response.getTotalPages()).isEqualTo(3);
  }

  @Test
  public void findAll() {
    ResponseEntity<List<LogicalMeterDto>> response = asElvacoUser()
      .getList("/meters/all", LogicalMeterDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(55);

    Long meterId = response.getBody().get(0).id;

    response = asElvacoUser()
      .getList("/meters/all?id=" + meterId, LogicalMeterDto.class);

    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0).id).isEqualTo(meterId);
  }

  /**
   * Test find all meters with a specific status entry,
   * excluding meters where that status is logged at a later period.
   */
  @Test
  public void findAllWithStatusUnderPeriodExcludeLate() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage(
        "/meters?after=2001-01-01T01:00:00.00Z"
          + "&before=2001-01-01T23:00:00.00Z"
          + "&status=Active",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    //TODO Meters are not sorted and order may differ. Improve assertion when meters are sorted
    assertThat(response.getContent().get(0).id)
      .as("Unexpected meter id at position 0")
      .isIn(meter1.logicalMeterId, meter2.logicalMeterId);
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isIn(meter1.logicalMeterId, meter2.logicalMeterId);
  }

  /**
   * Test find all meters with a specific status entry,
   * excluding meters where that status ended before the period.
   */
  @Test
  public void findAllWithStatusUnderPeriodExcludeEarly() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage(
        "/meters?after=2001-01-10T01:00:00.00Z"
          + "&before=2005-01-01T23:00:00.00Z"
          + "&status=Active",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);

    //TODO Meters are not sorted and order may differ. Improve assertion when meters are sorted
    assertThat(response.getContent().get(0).id)
      .as("Unexpected meter id at position 0 :" + response.getContent().get(0))
      .isIn(meter2.logicalMeterId, meter3.logicalMeterId, meter4.logicalMeterId);
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isIn(meter2.logicalMeterId, meter3.logicalMeterId, meter4.logicalMeterId);
    assertThat(response.getContent().get(2).id)
      .as("Unexpected meter id at position 2")
      .isIn(meter2.logicalMeterId, meter3.logicalMeterId, meter4.logicalMeterId);
  }

  /**
   * Test find all meters with a specific status entry,
   * and the status has no stop date.
   */
  @Test
  public void findAllWithStatusThatIsWithoutStopDate() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage(
        "/meters?after=2005-01-10T01:00:00.00Z"
          + "&before=2015-01-01T23:00:00.00Z"
          + "&status=Active",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(2)
      .as("Unexpected total count of elements");
    ;
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    //TODO Meters are not sorted and order may differ. Improve assertion when meters are sorted
    assertThat(response.getContent().get(0).id)
      .as("Unexpected meter id at position 0")
      .isIn(meter2.logicalMeterId, meter3.logicalMeterId);
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isIn(meter2.logicalMeterId, meter3.logicalMeterId);
  }

  @Test
  public void findAllWithPredicates() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage("/meters?medium=Hot water meter", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(5);
    assertThat(response.getNumberOfElements()).isEqualTo(5);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findAllMapDataForLogicalMeters() {
    ResponseEntity<List> response = asElvacoUser()
      .get("/meters/map-data", List.class);

    assertThat(response.getBody().size()).isEqualTo(55);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void findMeasurementsForLogicalMeter() {
    LogicalMeter savedLogicalMeter = logicalMeterRepository.save(
      new LogicalMeter("external-id", DomainModels.ELVACO.id, districtHeatingMeterDefinition)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(
      new PhysicalMeter(
        DomainModels.ELVACO,
        "111-222-333-444",
        "external-id",
        "Some device specific medium name",
        "ELV",
        savedLogicalMeter.id,
        Collections.emptyList()
      )
    );

    measurementUseCases.save(Arrays.asList(
      // We should find these
      new Measurement(Quantity.VOLUME, 2.0, "m^3", physicalMeter),
      new Measurement(Quantity.VOLUME, 3.1, "m^3", physicalMeter),
      new Measurement(Quantity.VOLUME, 4.0, "m^3", physicalMeter),
      new Measurement(Quantity.VOLUME, 5.0, "m^3", physicalMeter),
      new Measurement(Quantity.VOLUME, 5.2, "m^3", physicalMeter),

      // ... But not these, as they are of a quantity not defined in the meter definition
      new Measurement(Quantity.TEMPERATURE, 99, "°C", physicalMeter),
      new Measurement(Quantity.TEMPERATURE, 32, "°C", physicalMeter)
    ));

    Long meterId = savedLogicalMeter.id;

    ResponseEntity<List<MeasurementDto>> response = asElvacoUser()
      .getList("/meters/" + meterId + "/measurements", MeasurementDto.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<MeasurementDto> measurementDtos = response.getBody();
    assertThat(measurementDtos).hasSize(5);
    MeasurementDto measurement = measurementDtos.get(0);
    assertThat(measurement.quantity).isEqualTo(Quantity.VOLUME.getName());
    assertThat(measurement.unit).isEqualTo("m^3");
  }

  private LogicalMeter saveLogicalMeter(
    int seed,
    MeterDefinition meterDefinition
  ) {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    calendar.add(Calendar.DATE, seed);
    created = calendar.getTime();

    LogicalMeter logicalMeter = new LogicalMeter(
      null,
      "external-id-" + seed,
      DomainModels.ELVACO.id,
      new LocationBuilder().coordinate(new GeoCoordinate(1.1, 1.1, 1.0)).build(),
      created,
      Collections.emptyList(),
      meterDefinition,
      Collections.emptyList()
    );
    return logicalMeterRepository.save(logicalMeter);
  }

  private void prepareMeterLogsForStatusPeriodTest(
    PhysicalMeter meter1,
    PhysicalMeter meter2,
    PhysicalMeter meter3,
    PhysicalMeter meter4,
    MeterStatus meterStatus
  ) {
    List<MeterStatusLog> statuses = new ArrayList<>();

    statuses.add(new MeterStatusLog(
      null,
      meter1.id,
      meterStatus.id,
      meterStatus.name,
      statusLogDate,
      statusLogDate
    ));

    statuses.add(new MeterStatusLog(
      null,
      meter2.id,
      meterStatus.id,
      meterStatus.name,
      statusLogDate,
      null
    ));

    statuses.add(new MeterStatusLog(
      null,
      meter3.id,
      meterStatus.id,
      meterStatus.name,
      addDays(statusLogDate, 100),
      null
    ));

    statuses.add(new MeterStatusLog(
      null,
      meter4.id,
      meterStatus.id,
      meterStatus.name,
      addDays(statusLogDate, 100),
      addDays(statusLogDate, 1000)
    ));

    meterStatusLogs.save(statuses);
  }

  private Date addDays(Date date, int count) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, count);
    return calendar.getTime();
  }

  private void createStatusMockData() {
    meterStatuses.save(asList(
      new MeterStatus("Active")
    ));
  }

  private void createAndConnectPhysicalMeters(List<LogicalMeter> logicalMeters) {
    logicalMeters.forEach(logicalMeter -> createPhysicalMeter(logicalMeter.id, logicalMeter.id));
  }

  private void createPhysicalMeter(long logicalMeterId, long seed) {
    physicalMeters.save(
      new PhysicalMeter(
        DomainModels.ELVACO,
        "111-222-333-444-" + seed,
        "external-id-" + seed,
        "Some device specific medium name",
        "ELV",
        logicalMeterId,
        Collections.emptyList()
      )
    );
  }

}
