package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.configuration.bootstrap.demo.DemoDataHelper;
import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
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

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.StatusType.ACTIVE;
import static com.elvaco.mvp.core.domainmodels.StatusType.INFO;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static com.elvaco.mvp.testing.util.DateHelper.utcZonedDateTimeOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterControllerTest extends IntegrationTest {

  private final ZonedDateTime meter1ActiveDate =
    ZonedDateTime.parse("2001-01-01T10:14:00.00Z");

  private final ZonedDateTime meter1FirstMeasurement =
    ZonedDateTime.parse("2001-01-01T11:00:00.00Z");

  private final ZonedDateTime meter2FirstMeasurement =
    ZonedDateTime.parse("2001-01-01T10:15:00.00Z");

  private final ZonedDateTime statusLogDate = ZonedDateTime.parse("2001-01-01T10:14:00.00Z");
  private PhysicalMeter physicalMeter1;
  private PhysicalMeter physicalMeter2;
  private PhysicalMeter physicalMeter3;
  private PhysicalMeter physicalMeter4;
  private PhysicalMeter physicalMeter5;
  private MeterDefinition districtHeatingMeterDefinition;
  private MeterDefinition hotWaterMeterDefinition;

  @Autowired
  private LogicalMeters logicalMeterRepository;
  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;
  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;
  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;
  @Autowired
  private MeasurementUseCases measurementUseCases;
  @Autowired
  private MeterDefinitions meterDefinitions;
  @Autowired
  private PhysicalMeters physicalMeters;
  @Autowired
  private MeterStatusLogs meterStatusLogs;
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
        "another-organisation",
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

    createAndConnectPhysicalMeters(logicalMeterRepository.findAll(new RequestParametersAdapter()));

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

    prepareMeterLogsForStatusPeriodTest(
      physicalMeter1,
      physicalMeter2,
      physicalMeter3,
      physicalMeter4,
      physicalMeter5
    );
  }

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    organisationJpaRepository.delete(anotherOrganisation.id);
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void findAllWithCollectionStatusPaged() {
    // Change to hourly measurements
    PhysicalMeter physicalMeter1Hourly = physicalMeters.save(new PhysicalMeter(
      physicalMeter1.id,
      physicalMeter1.organisation,
      physicalMeter1.address,
      physicalMeter1.externalId,
      physicalMeter1.medium,
      physicalMeter1.manufacturer,
      physicalMeter1.logicalMeterId,
      60,
      null
    ));

    addMeasurements(
      physicalMeter1Hourly,
      physicalMeter2
    );

    Page<LogicalMeterDto> response = as(context().user)
      .getPage(
        "/meters"
        + "?after=2001-01-01T01:00:00.00Z"
        + "&before=2002-01-01T00:00:00.00Z"
        + "&status=active"
        + "&sort=id,asc",
        LogicalMeterDto.class
      );

    assertCollectionStatus(response.getContent().get(0), response.getContent().get(1));
  }

  @Test
  public void findById() {
    ResponseEntity<LogicalMeterDto> response = as(context().user)
      .get("/meters/" + physicalMeter1.logicalMeterId, LogicalMeterDto.class);

    LogicalMeterDto logicalMeterDto = response.getBody();

    assertThatStatusIsOk(response);
    assertThat(logicalMeterDto.id)
      .as("Unexpected meter id")
      .isEqualTo(physicalMeter1.logicalMeterId);

    assertThat(logicalMeterDto.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(2);

    MeterStatusLogDto meterStatusLogDto = logicalMeterDto.statusChangelog.get(0);

    assertThat(meterStatusLogDto.start)
      .as("Unexpected date format for status start")
      .isEqualTo("2001-01-01 10:14:00");

    assertThat(meterStatusLogDto.stop)
      .as("Unexpected date format for status stop")
      .isEqualTo("2001-01-06 10:14:00");
  }

  @Test
  public void findAllPaged() {
    Page<LogicalMeterDto> response = as(context().user)
      .getPage("/meters", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(20);
    assertThat(response.getTotalPages()).isEqualTo(3);

    response = as(context().user)
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

  /**
   * Test find all meters with a specific status entry,
   * excluding meters where that status is logged at a later period.
   */
  @Test
  public void findAllWithStatusUnderPeriodExcludeLate() {
    Page<LogicalMeterDto> response = as(context().user)
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
      .isEqualTo(physicalMeter1.logicalMeterId);
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isEqualTo(physicalMeter2.logicalMeterId);

    LogicalMeterDto actualMeter5 = response.getContent().get(2);

    assertThat(actualMeter5.id)
      .as("Unexpected meter id at position 2")
      .isEqualTo(physicalMeter5.logicalMeterId);

    assertThat(actualMeter5.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(1);

    assertThat(actualMeter5.statusChangelog.get(0).start)
      .as("Unexpected date of first log entry")
      .isEqualTo("2001-01-01 10:14:00");
  }

  /**
   * Test find all meters with a specific status entry,
   * excluding meters where that status ended before the period.
   */
  @Test
  public void findAllWithStatusUnderPeriodExcludeEarly() {
    Page<LogicalMeterDto> response = as(context().user)
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
      .isEqualTo(physicalMeter2.logicalMeterId);
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isEqualTo(physicalMeter3.logicalMeterId);
    assertThat(response.getContent().get(2).id)
      .as("Unexpected meter id at position 2")
      .isEqualTo(physicalMeter4.logicalMeterId);
    assertThat(response.getContent().get(3).id)
      .as("Unexpected meter id at position 3")
      .isEqualTo(physicalMeter5.logicalMeterId);
  }

  @Test
  public void findAllWithStatusUnderPeriodExcludeEarlyAndLate() {
    Page<LogicalMeterDto> response = as(context().user)
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
      .isEqualTo(physicalMeter5.logicalMeterId);

    assertThat(logicalMeter.statusChangelog.size())
      .as("Unexpected number of log entries")
      .isEqualTo(11);

    assertThat(logicalMeter.statusChangelog.get(0).start)
      .as("Unexpected date of first log entry")
      .isEqualTo("2001-01-20 10:14:00");

    assertThat(logicalMeter.statusChangelog.get(10).start)
      .as("Unexpected date of last log entry")
      .isEqualTo("2001-01-10 10:14:00");

    assertThat(logicalMeter.status).isEqualTo(ACTIVE);
    assertThat(logicalMeter.statusChanged).isEqualTo("2001-01-20 10:14:00");
  }

  /**
   * Test find all meters with a specific status entry,
   * and the status has no stop date.
   */
  @Test
  public void findAllWithStatusThatIsWithoutStopDate() {
    Page<LogicalMeterDto> response = as(context().user)
      .getPage(
        "/meters?after=2005-01-10T01:00:00.00Z"
        + "&before=2015-01-01T23:00:00.00Z"
        + "&status=active"
        + "&size=20"
        + "&page=0"
        + "&sort=id,asc",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements())
      .as("Unexpected total count of elements")
      .isEqualTo(2);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertThat(response.getContent().get(0).id)
      .as("Unexpected meter id at position 0")
      .isEqualTo(physicalMeter2.logicalMeterId);
    assertThat(response.getContent().get(1).id)
      .as("Unexpected meter id at position 1")
      .isEqualTo(physicalMeter3.logicalMeterId);
  }

  @Test
  public void malformedDateParameter() {
    ResponseEntity<ErrorMessageDto> response = as(context().user)
      .get(
        "/meters?"
        + "after=NotAValidTimestamp"
        + "&before=AndNeitherIsThis",
        ErrorMessageDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Failed to construct filter 'after' for value 'NotAValidTimestamp'");
  }

  @Test
  public void findAllWithPredicates() {
    Page<LogicalMeterDto> response = as(context().user)
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
      context().organisation().id,
      hotWaterMeterDefinition,
      UNKNOWN_LOCATION
    ));

    Page<LogicalMeterDto> response = as(context().user)
      .getPage("/meters?organisation=" + context().organisation().id, LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(1L);
  }

  @Test
  public void cannotAccessOtherOrganisationsMetersByFilter() {
    createUserIfNotPresent(userBuilder().build());

    logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "not-my-meter",
      context().organisation().id,
      hotWaterMeterDefinition,
      UNKNOWN_LOCATION
    ));

    Page<LogicalMeterDto> response = restClient()
      .loginWith("me@myorg.com", "secr3t")
      .tokenAuthorization()
      .getPage("/meters?organisation=" + context().organisation().id, LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0L);
  }

  @Test
  public void doesntFindOtherOrganisationsMetersUsingFilter() {
    createUserIfNotPresent(userBuilder().build());
    LogicalMeter myMeter = logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "my-own-meter",
      anotherOrganisation.id,
      hotWaterMeterDefinition,
      UNKNOWN_LOCATION
    ));
    logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "not-my-meter",
      context().organisation().id,
      hotWaterMeterDefinition,
      UNKNOWN_LOCATION
    ));

    Page<LogicalMeterDto> response = restClient()
      .loginWith("me@myorg.com", "secr3t")
      .tokenAuthorization()
      .getPage("/meters?medium=Hot water meter", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1L);
    assertThat(response.getContent().get(0).id).isEqualTo(myMeter.id);
  }

  @Test
  public void cantAccessOtherOrganisationsMeterById() {
    LogicalMeter theirMeter = logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "this-is-not-my-meter",
      anotherOrganisation.id,
      hotWaterMeterDefinition,
      UNKNOWN_LOCATION
    ));

    ResponseEntity<ErrorMessageDto> response = as(context().user)
      .get("/meters/" + theirMeter.id, ErrorMessageDto.class);

    assertThatStatusIsNotFound(response);
  }

  @Test
  public void meterNotFound() {
    ResponseEntity<ErrorMessageDto> response = as(context().user)
      .get("/meters/" + randomUUID(), ErrorMessageDto.class);

    assertThatStatusIsNotFound(response);
  }

  @Test
  public void findAllMapDataForLogicalMeters() {
    ResponseEntity<List<MapMarkerDto>> response = as(context().user)
      .getList("/meters/map-markers", MapMarkerDto.class);

    assertThatStatusIsOk(response);
    assertThat(response.getBody().size()).isEqualTo(55);
  }

  @Test
  public void findAllMapDataForLogicalMetersWithParameters() {
    ResponseEntity<List<MapMarkerDto>> response = as(context().user)
      .getList("/meters/map-markers?city=sweden,varberg", MapMarkerDto.class);

    assertThatStatusIsOk(response);
    assertThat(response.getBody().size()).isEqualTo(27);
  }

  @Test
  public void findMeasurementsForLogicalMeter() {
    LogicalMeter savedLogicalMeter = logicalMeterRepository.save(
      new LogicalMeter(
        randomUUID(),
        "external-id",
        context().organisation().id,
        districtHeatingMeterDefinition,
        UNKNOWN_LOCATION
      )
    );

    PhysicalMeter physicalMeter = physicalMeters.save(
      new PhysicalMeter(
        randomUUID(),
        context().organisation(),
        "111-222-333-444",
        "external-id",
        "Some device specific medium name",
        "ELV",
        savedLogicalMeter.id,
        15L,
        null
      )
    );

    measurementUseCases.save(asList(
      // We should find these
      new Measurement(Quantity.VOLUME, 2.0, physicalMeter),
      new Measurement(Quantity.VOLUME, 3.1, physicalMeter),
      new Measurement(Quantity.VOLUME, 4.0, physicalMeter),
      new Measurement(Quantity.VOLUME, 5.0, physicalMeter),
      new Measurement(Quantity.VOLUME, 5.2, physicalMeter),

      // ... But not these, as they are of a quantity not defined in the meter definition
      new Measurement(Quantity.TEMPERATURE, 99, physicalMeter),
      new Measurement(Quantity.TEMPERATURE, 32, physicalMeter)
    ));

    UUID meterId = savedLogicalMeter.id;

    ResponseEntity<List<MeasurementDto>> response = as(context().user)
      .getList("/meters/" + meterId + "/measurements", MeasurementDto.class);

    List<MeasurementDto> measurementDtos = response.getBody();
    MeasurementDto measurement = measurementDtos.get(0);

    assertThatStatusIsOk(response);
    assertThat(measurementDtos).hasSize(5);
    assertThat(measurement.quantity).isEqualTo(Quantity.VOLUME.name);
    assertThat(measurement.unit).isEqualTo("m³");
  }

  private void assertCollectionStatus(
    LogicalMeterDto logicalMeter1,
    LogicalMeterDto logicalMeter2
  ) {
    assertThat(logicalMeter1.id)
      .as("Unexpected meter id")
      .isEqualTo(physicalMeter1.logicalMeterId);

    assertThat(logicalMeter1.collectionStatus)
      .as("Unexpected collection status")
      .isEqualTo("91.59663865546219");

    assertThat(logicalMeter2.id)
      .as("Unexpected meter id")
      .isEqualTo(physicalMeter2.logicalMeterId);

    assertThat(logicalMeter2.collectionStatus)
      .as("Unexpected collection status")
      .isEqualTo("100.0");
  }

  private void assertThatStatusIsNotFound(ResponseEntity<ErrorMessageDto> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private void assertThatStatusIsOk(ResponseEntity<?> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private UserBuilder userBuilder() {
    return new UserBuilder()
      .name("Me")
      .email("me@myorg.com")
      .password("secr3t")
      .organisation(new Organisation(
        anotherOrganisation.id,
        anotherOrganisation.name,
        anotherOrganisation.slug
      ))
      .asUser();
  }

  private void testSorting(
    String url,
    String errorMessage,
    Function<LogicalMeterDto, String> actual,
    String expected
  ) {
    Page<LogicalMeterDto> response = as(context().user)
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
    ZonedDateTime created = utcZonedDateTimeOf("2001-01-01T10:14:00.00Z").plusDays(seed);

    LogicalMeter logicalMeter = new LogicalMeter(
      randomUUID(),
      "external-id-" + seed,
      context().organisation().id,
      new LocationBuilder()
        .country("sweden")
        .city(city)
        .streetAddress(streetAddress)
        .coordinate(new GeoCoordinate(1.1, 123.12))
        .build(),
      created,
      emptyList(),
      meterDefinition,
      emptyList()
    );
    return logicalMeterRepository.save(logicalMeter);
  }

  private void prepareMeterLogsForStatusPeriodTest(
    PhysicalMeter meter1,
    PhysicalMeter meter2,
    PhysicalMeter meter3,
    PhysicalMeter meter4,
    PhysicalMeter meter5
  ) {
    List<MeterStatusLog> meterStatusLogs = new ArrayList<>();

    meterStatusLogs.add(new MeterStatusLog(
      null,
      meter1.id,
      ACTIVE,
      meter1ActiveDate,
      meter1ActiveDate.plusDays(5L)
    ));

    meterStatusLogs.add(new MeterStatusLog(
      null,
      meter1.id,
      INFO,
      statusLogDate,
      statusLogDate
    ));

    meterStatusLogs.add(new MeterStatusLog(
      null,
      meter2.id,
      ACTIVE,
      statusLogDate,
      null
    ));

    meterStatusLogs.add(new MeterStatusLog(
      null,
      meter3.id,
      ACTIVE,
      statusLogDate.plusDays(100L),
      null
    ));

    meterStatusLogs.add(new MeterStatusLog(
      null,
      meter4.id,
      ACTIVE,
      statusLogDate.plusDays(100),
      statusLogDate.plusDays(1000)
    ));

    meterStatusLogs.add(new MeterStatusLog(
      null,
      meter4.id,
      WARNING,
      statusLogDate.plusDays(100),
      statusLogDate.plusDays(1000)
    ));

    for (int x = 0; x < 50; x++) {
      meterStatusLogs.add(new MeterStatusLog(
        null,
        meter5.id,
        ACTIVE,
        statusLogDate.plusDays(x),
        statusLogDate.plusDays(x)
      ));
    }

    this.meterStatusLogs.save(meterStatusLogs);
  }

  private void addMeasurements(
    PhysicalMeter meter1,
    PhysicalMeter meter2
  ) {

    List<MeasurementEntity> meter1Measurements = createMeasurements(
      meter1FirstMeasurement,
      meter1.readIntervalMinutes,
      119,
      new PhysicalMeterEntity(meter1.id),
      true
    );

    List<MeasurementEntity> measurements = new ArrayList<>();
    measurements.addAll(meter1Measurements);
    measurements.addAll(
      createMeasurements(
        meter2FirstMeasurement,
        meter2.readIntervalMinutes,
        36000,
        new PhysicalMeterEntity(meter2.id),
        false
      )
    );
    measurementJpaRepository.save(measurements);
  }

  /**
   * Creates a list of fake measurements.
   *
   * @param interval      Time in minutes between measurements
   * @param values        Nr of values to generate
   * @param physicalMeter
   *
   * @return
   */
  private List<MeasurementEntity> createMeasurements(
    ZonedDateTime measurementDate,
    long interval,
    long values,
    PhysicalMeterEntity physicalMeter,
    boolean skippMeasurements
  ) {
    List<MeasurementEntity> measurementEntities = new ArrayList<>();
    for (int x = 0; x < values; x++) {
      if (((x >= 10 && x < 15) || (x >= 25 && x < 30)) && skippMeasurements) {
        //Simulate missing readings
        continue;
      }

      measurementEntities.addAll(
        DemoDataHelper.getDistrictHeatingMeterReading(
          measurementDate.plusMinutes(x * interval),
          physicalMeter
        )
      );
    }

    return measurementEntities;
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
        context().organisation(),
        "111-222-333-444-1",
        externalId,
        "Some device specific medium name",
        "ELV1",
        logicalMeterId,
        15,
        null
      )
    );
  }
}
