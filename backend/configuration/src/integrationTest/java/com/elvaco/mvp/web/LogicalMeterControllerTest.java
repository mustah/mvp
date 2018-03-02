package com.elvaco.mvp.web;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterStatus;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatuses;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterStatusJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import com.elvaco.mvp.web.util.Dates;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class LogicalMeterControllerTest extends IntegrationTest {

  private static int seed = 1;

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
  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  private OrganisationEntity anotherOrganisation;

  @Before
  public void setUp() {
    districtHeatingMeterDefinition = meterDefinitions.save(
      MeterDefinition.DISTRICT_HEATING_METER
    );
    hotWaterMeterDefinition = meterDefinitions.save(
      MeterDefinition.HOT_WATER_METER
    );

    anotherOrganisation = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Another Organisation",
        "another-organisation"
      ));

    for (int seed = 1; seed <= 55; seed++) {
      MeterDefinition meterDefinition = seed % 10 == 0
        ? hotWaterMeterDefinition
        : districtHeatingMeterDefinition;

      String city = seed % 2 == 0 ? "Varberg" : "Östersund";
      String streetAddress = seed % 2 == 0 ? "Drottninggatan " + seed : "Kungsgatan " + seed;
      saveLogicalMeter(seed, meterDefinition, streetAddress, city);
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
    organisationJpaRepository.delete(anotherOrganisation.id);
  }

  @Test
  public void findById() {
    ResponseEntity<LogicalMeterDto> response = asElvacoUser()
      .get("/meters/" + meter1.logicalMeterId, LogicalMeterDto.class);

    LogicalMeterDto logicalMeterDto = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(logicalMeterDto.id)
      .as("Unexpected meter id")
      .isEqualTo(meter1.logicalMeterId.toString());

    assertThat(logicalMeterDto.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(1);

    MeterStatusLogDto meterStatusLogDto = logicalMeterDto.statusChangelog.get(0);
    String formatTime = Dates.formatTime(statusLogDate, TimeZone.getDefault());

    assertThat(meterStatusLogDto.start).as("Unexpected date format").isEqualTo(formatTime);
    assertThat(meterStatusLogDto.stop).as("Unexpected date format").isEqualTo(formatTime);
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

  @Ignore
  @Test
  public void findAllPagedAndSorted() {
    // Address asc
    testSorting(
      "/meters?size=20&page=0&sort=address,asc",
      "Unexpected address, sorting failed",
      (LogicalMeterDto meter) -> meter.address.name,
      "Drottninggatan 2"
    );

    // Address desc
    testSorting(
      "/meters?size=20&page=0&sort=address,desc",
      "Unexpected address, sorting failed",
      (LogicalMeterDto meter) -> meter.address.name,
      "Kungsgatan 55"
    );

    // Manufacturer asc
    testSorting(
      "/meters?size=20&page=0&sort=manufacturer,asc",
      "Unexpected manufacturer, sorting failed",
      (LogicalMeterDto meter) -> meter.manufacturer,
      "ELV1"
    );

    // Manufacturer desc
    testSorting(
      "/meters?size=20&page=0&sort=manufacturer,desc",
      "Unexpected manufacturer, sorting failed",
      (LogicalMeterDto meter) -> meter.manufacturer,
      "ELV55"
    );

    testSorting(
      "/meters?size=20&page=0&sort=city,asc",
      "Unexpected city, sorting failed",
      (LogicalMeterDto meter) -> meter.city.name,
      "Varberg"
    );

    testSorting(
      "/meters?size=20&page=0&sort=city,desc",
      "Unexpected city, sorting failed",
      (LogicalMeterDto meter) -> meter.city.name,
      "Östersund"
    );
  }

  private void testSorting(
    String url,
    String errorMessage,
    Function<LogicalMeterDto, String> actual,
    String expected
  ) {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage(url, LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);

    assertThat(actual.apply(response.getContent().get(0)))
      .as(errorMessage)
      .isEqualTo(expected);
  }

  @Test
  public void findAll() {
    ResponseEntity<List<LogicalMeterDto>> response = asElvacoUser()
      .getList("/meters/all", LogicalMeterDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(55);

    String meterId = response.getBody().get(0).id;

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
      .isIn(meter1.logicalMeterId.toString(), meter2.logicalMeterId.toString());
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isIn(meter1.logicalMeterId.toString(), meter2.logicalMeterId.toString());
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
      .isIn(
        meter2.logicalMeterId.toString(),
        meter3.logicalMeterId.toString(),
        meter4.logicalMeterId.toString()
      );
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isIn(
        meter2.logicalMeterId.toString(),
        meter3.logicalMeterId.toString(),
        meter4.logicalMeterId.toString()
      );
    assertThat(response.getContent().get(2).id)
      .as("Unexpected meter id at position 2")
      .isIn(
        meter2.logicalMeterId.toString(),
        meter3.logicalMeterId.toString(),
        meter4.logicalMeterId.toString()
      );
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
      .isIn(meter2.logicalMeterId.toString(), meter3.logicalMeterId.toString());
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isIn(meter2.logicalMeterId.toString(), meter3.logicalMeterId.toString());
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
  public void findsOwnOrganisationsMetersByFilter() {
    logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "my-meter",
      ELVACO.id,
      hotWaterMeterDefinition
    ));

    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage("/meters?organisation=" + ELVACO.id, LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(1L);
  }

  @Test
  public void cantAccessOtherOrganisationsMetersByFilter() {
    createUserIfNotPresent(new User(
      "Me",
      "me@myorg.com",
      "secr3t",
      new Organisation(anotherOrganisation.id, anotherOrganisation.name, anotherOrganisation.code),
      singletonList(
        Role.USER)
    ));

    logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "not-my-meter",
      ELVACO.id,
      hotWaterMeterDefinition
    ));

    Page<LogicalMeterDto> response = restClient()
      .loginWith("me@myorg.com", "secr3t")
      .tokenAuthorization()
      .getPage("/meters?organisation=" + ELVACO.id, LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0L);
  }

  @Test
  public void doesntFindOtherOrganisationsMetersUsingFilter() {
    createUserIfNotPresent(new User(
      "Me",
      "me@myorg.com",
      "secr3t",
      new Organisation(anotherOrganisation.id, anotherOrganisation.name, anotherOrganisation.code),
      singletonList(Role.USER)
    ));
    LogicalMeter myMeter = logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "my-own-meter",
      anotherOrganisation.id,
      hotWaterMeterDefinition
    ));
    logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "not-my-meter",
      ELVACO.id,
      hotWaterMeterDefinition
    ));

    Page<LogicalMeterDto> response = restClient()
      .loginWith("me@myorg.com", "secr3t")
      .tokenAuthorization()
      .getPage("/meters?medium=Hot water meter", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1L);
    assertThat(response.getContent().get(0).id).isEqualTo(myMeter.id.toString());
  }

  @Test
  public void cantAccessOtherOrganisationsMeterById() {
    LogicalMeter theirMeter = logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "this-is-not-my-meter",
      anotherOrganisation.id,
      hotWaterMeterDefinition
    ));

    ResponseEntity<ErrorMessageDto> response = asElvacoUser()
      .get("/meters/" + theirMeter.id, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void meterNotFound() {
    ResponseEntity<ErrorMessageDto> response = asElvacoUser()
      .get("/meters/" + randomUUID(), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
      new LogicalMeter(randomUUID(), "external-id", ELVACO.id, districtHeatingMeterDefinition)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(
      new PhysicalMeter(
        randomUUID(),
        ELVACO,
        "111-222-333-444",
        "external-id",
        "Some device specific medium name",
        "ELV",
        savedLogicalMeter.id
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

    UUID meterId = savedLogicalMeter.id;

    ResponseEntity<List<MeasurementDto>> response = asElvacoUser()
      .getList("/meters/" + meterId + "/measurements", MeasurementDto.class);

    List<MeasurementDto> measurementDtos = response.getBody();
    MeasurementDto measurement = measurementDtos.get(0);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(measurementDtos).hasSize(5);
    assertThat(measurement.quantity).isEqualTo(Quantity.VOLUME.name);
    assertThat(measurement.unit).isEqualTo("m^3");
  }

  private LogicalMeter saveLogicalMeter(
    int seed,
    MeterDefinition meterDefinition,
    String streetAddress,
    String city
  ) {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    calendar.add(Calendar.DATE, seed);
    created = calendar.getTime();

    LogicalMeter logicalMeter = new LogicalMeter(
      randomUUID(),
      "external-id-" + seed,
      ELVACO.id,
      new LocationBuilder()
        .city(city)
        .streetAddress(streetAddress)
        .coordinate(new GeoCoordinate(1.1, 1.1, 1.0))
        .build(),
      created,
      emptyList(),
      meterDefinition,
      emptyList(),
      emptyList()
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
    logicalMeters.forEach(logicalMeter -> createPhysicalMeter(
      logicalMeter.id,
      logicalMeter.externalId
    ));
  }

  private void createPhysicalMeter(UUID logicalMeterId, String externalId) {
    physicalMeters.save(
      new PhysicalMeter(
        randomUUID(),
        ELVACO,
        "111-222-333-444-" + seed,
        externalId,
        "Some device specific medium name",
        "ELV" + seed,
        logicalMeterId
      )
    );
  }
}
