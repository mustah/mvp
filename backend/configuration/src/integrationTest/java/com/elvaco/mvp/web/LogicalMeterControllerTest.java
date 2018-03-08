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

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
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
import com.elvaco.mvp.core.domainmodels.Status;
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
import com.elvaco.mvp.testing.fixture.UserBuilder;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class LogicalMeterControllerTest extends IntegrationTest {

  private static int seed = 1;

  private final Date statusLogDate = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

  PhysicalMeter physicalMeter1;
  PhysicalMeter physicalMeter2;
  PhysicalMeter physicalMeter3;
  PhysicalMeter physicalMeter4;
  PhysicalMeter physicalMeter5;

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

    com.elvaco.mvp.core.spi.data.Page<LogicalMeter> meters =
      logicalMeterRepository.findAll(
        new RequestParametersAdapter(),
        new PageableAdapter(new PageRequest(0, 5, Direction.ASC, "id"))
      );

    physicalMeter1 = meters.getContent().get(0).physicalMeters.get(0);
    physicalMeter2 = meters.getContent().get(1).physicalMeters.get(0);
    physicalMeter3 = meters.getContent().get(2).physicalMeters.get(0);
    physicalMeter4 = meters.getContent().get(3).physicalMeters.get(0);
    physicalMeter5 = meters.getContent().get(4).physicalMeters.get(0);

    MeterStatus meterStatus = meterStatuses.findAll().get(0);
    prepareMeterLogsForStatusPeriodTest(
      physicalMeter1,
      physicalMeter2,
      physicalMeter3,
      physicalMeter4,
      physicalMeter5,
      meterStatus
    );
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
      .get("/meters/" + physicalMeter1.logicalMeterId, LogicalMeterDto.class);

    LogicalMeterDto logicalMeterDto = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(logicalMeterDto.id)
      .as("Unexpected meter id")
      .isEqualTo(physicalMeter1.logicalMeterId.toString());

    assertThat(logicalMeterDto.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(1);

    MeterStatusLogDto meterStatusLogDto = logicalMeterDto.statusChangelog.get(0);
    String formatTime = Dates.formatTime(statusLogDate, TimeZone.getDefault());

    assertThat(meterStatusLogDto.start)
      .as("Unexpected date format")
      .isEqualTo(formatTime);

    assertThat(meterStatusLogDto.stop)
      .as("Unexpected date format")
      .isEqualTo(formatTime);
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
      (LogicalMeterDto meter) -> meter.location.address.name,
      "Drottninggatan 2"
    );

    // Address desc
    testSorting(
      "/meters?size=20&page=0&sort=address,desc",
      "Unexpected address, sorting failed",
      (LogicalMeterDto meter) -> meter.location.address.name,
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
      (LogicalMeterDto meter) -> meter.location.city.name,
      "Varberg"
    );

    testSorting(
      "/meters?size=20&page=0&sort=city,desc",
      "Unexpected city, sorting failed",
      (LogicalMeterDto meter) -> meter.location.city.name,
      "Östersund"
    );
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
        + "&status=active"
        + "&size=20"
        + "&page=0"
        + "&sort=id,asc",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertThat(response.getContent().get(0).id)
      .as("Unexpected meter id at position 0")
      .isEqualTo(physicalMeter1.logicalMeterId.toString());
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isEqualTo(physicalMeter2.logicalMeterId.toString());

    LogicalMeterDto actualMeter5 = response.getContent().get(2);

    assertThat(actualMeter5.id)
      .as("Unexpected meter id at position 2")
      .isEqualTo(physicalMeter5.logicalMeterId.toString());

    assertThat(actualMeter5.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(1);

    assertThat(actualMeter5.statusChangelog.get(0).start)
      .as("Unexpected date of first log entry")
      .isEqualTo(timeZoneMagic("2001-01-01T10:14:00.00Z"));
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
        + "&status=active"
        + "&size=20"
        + "&page=0"
        + "&sort=id,asc",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getNumberOfElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertThat(response.getContent().get(0).id)
      .as("Unexpected meter id at position 0 :" + response.getContent().get(0))
      .isEqualTo(physicalMeter2.logicalMeterId.toString());
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isEqualTo(physicalMeter3.logicalMeterId.toString());
    assertThat(response.getContent().get(2).id)
      .as("Unexpected meter id at position 2")
      .isEqualTo(physicalMeter4.logicalMeterId.toString());
    assertThat(response.getContent().get(3).id)
      .as("Unexpected meter id at position 3")
      .isEqualTo(physicalMeter5.logicalMeterId.toString());
  }

  @Test
  public void findAllWithStatusUnderPeriodExcludeEarlyAndLate() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage(
        "/meters?after=2001-01-10T01:00:00.00Z"
        + "&before=2001-01-20T23:00:00.00Z"
        + "&status=active"
        + "&size=20"
        + "&page=0"
        + "&sort=id,asc",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    LogicalMeterDto logicalMeter = response.getContent().get(1);

    assertThat(logicalMeter.id)
      .as("Unexpected meter id at position 3")
      .isEqualTo(physicalMeter5.logicalMeterId.toString());

    assertThat(logicalMeter.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(11);

    assertThat(logicalMeter.statusChangelog.get(0).start)
      .as("Unexpected date of first log entry")
      .isEqualTo(timeZoneMagic("2001-01-20T10:14:00.00Z"));

    assertThat(logicalMeter.statusChangelog.get(10).start)
      .as("Unexpected date of last log entry")
      .isEqualTo(timeZoneMagic("2001-01-10T10:14:00.00Z"));

    assertThat(logicalMeter.status).isEqualTo(Status.ACTIVE);
    assertThat(logicalMeter.statusChanged).isEqualTo(timeZoneMagic("2001-01-20T10:14:00.00Z"));
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
        + "&status=active"
        + "&size=20"
        + "&page=0"
        + "&sort=id,asc",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(2)
      .as("Unexpected total count of elements");
    ;
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertThat(response.getContent().get(0).id)
      .as("Unexpected meter id at position 0")
      .isEqualTo(physicalMeter2.logicalMeterId.toString());
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isEqualTo(physicalMeter3.logicalMeterId.toString());
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
  public void cannotAccessOtherOrganisationsMetersByFilter() {
    createUserIfNotPresent(userBuilder().build());

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
    createUserIfNotPresent(userBuilder().build());
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

  private UserBuilder userBuilder() {
    return new UserBuilder()
      .name("Me")
      .email("me@myorg.com")
      .password("secr3t")
      .organisation(new Organisation(
        anotherOrganisation.id,
        anotherOrganisation.name,
        anotherOrganisation.code
      ))
      .asUser();
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
        .coordinate(new GeoCoordinate(1.1, 123.12))
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
    PhysicalMeter meter5,
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

    for (int x = 0; x < 50; x++) {
      statuses.add(new MeterStatusLog(
        null,
        meter5.id,
        meterStatus.id,
        meterStatus.name,
        addDays(statusLogDate, x),
        addDays(statusLogDate, x)
      ));
    }

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
      new MeterStatus("active")
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

  private String timeZoneMagic(String date) {
    return Dates.formatTime(Date.from(Instant.parse(date)), TimeZone.getDefault());
  }
}
