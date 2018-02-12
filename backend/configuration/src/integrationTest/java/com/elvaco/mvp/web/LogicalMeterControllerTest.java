package com.elvaco.mvp.web;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterStatus;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.UserProperty;
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
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
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

  private final long statusLogStart = new Date().getTime();
  private final long statusLogStop = new Date().getTime();

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
    for (int seed = 1; seed <= 55; seed++) {
      String status = seed % 10 == 0 ? "Warning" : "Ok";
      saveLogicalMeter(seed, status);
    }

    createAndConnectPhysicalMeters(logicalMeterRepository.findAll());

    createStatusMockData();

    addLogToMeters();
  }

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
    meterStatusJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterRepository.deleteAll();

    restClient().logout();
  }

  @Test
  public void findById() {
    List<LogicalMeter> logicalMeters = logicalMeterRepository.findAll();

    ResponseEntity<LogicalMeterDto> response = asElvacoUser()
      .get("/meters/" + logicalMeters.get(0).id, LogicalMeterDto.class);

    LogicalMeterDto logicalMeterDto = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(logicalMeterDto.id).isEqualTo(logicalMeters.get(0).id);

    assertThat(logicalMeterDto.statusChangelog.size()).isEqualTo(3);

    assertThat(logicalMeterDto.statusChangelog.get(0).start).isEqualTo(new Date(statusLogStart));
    assertThat(logicalMeterDto.statusChangelog.get(0).stop).isEqualTo(new Date(statusLogStop));
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

  @Test
  public void findAllWithinPeriod() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage(
        "/meters?before=2001-01-20T10:10:00.00Z&after=2001-01-10T10:10:00.00Z",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(10);
    assertThat(response.getNumberOfElements()).isEqualTo(10);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findAllWithPredicates() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage("/meters?status=Warning", LogicalMeterDto.class);

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
    MeterDefinition districtHeatingMeterDefinition =
      meterDefinitions.save(MeterDefinition.DISTRICT_HEATING_METER);

    LogicalMeter savedLogicalMeter = logicalMeterRepository.save(
      new LogicalMeter(districtHeatingMeterDefinition)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(
      new PhysicalMeter(
        DomainModels.ELVACO,
        "111-222-333-444",
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

  @Test
  public void findStatusLogForLogicalMeter() {
    List<LogicalMeter> logicalMeters = logicalMeterRepository.findAll();

    Page<MeterStatusLogDto> response = asElvacoUser()
      .getPage("/meters/" + logicalMeters.get(0).id + "/status", MeterStatusLogDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);

  }

  private LogicalMeter saveLogicalMeter(int seed, String status) {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    calendar.add(Calendar.DATE, seed);
    created = calendar.getTime();

    LogicalMeter logicalMeter = new LogicalMeter(
      null,
      status,
      new LocationBuilder().coordinate(new GeoCoordinate(1.1, 1.1, 1.0)).build(),
      created,
      new PropertyCollection(new UserProperty("abc123", "Some project"))
    );
    return logicalMeterRepository.save(logicalMeter);
  }

  private void addLogToMeters() {
    List<MeterStatusLog> statuses = new ArrayList<>();

    physicalMeters.findAll().forEach(
      physicalMeter ->
        meterStatuses.findAll().forEach(
          meterStatus ->
            statuses.add(
              new MeterStatusLog(
                null,
                physicalMeter.id,
                meterStatus.id,
                meterStatus.name,
                new Date(statusLogStart),
                new Date(statusLogStop)
              )
            )
        )
    );

    meterStatusLogs.save(statuses);
  }

  private void createStatusMockData() {
    meterStatuses.save(asList(
      new MeterStatus("Active"),
      new MeterStatus("Inactive"),
      new MeterStatus("Maintenance scheduled")
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
        "Some device specific medium name",
        "ELV",
        logicalMeterId,
        Collections.emptyList()
      )
    );
  }

}
