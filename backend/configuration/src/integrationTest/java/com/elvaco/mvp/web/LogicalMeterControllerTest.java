package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterControllerTest extends IntegrationTest {

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
  public void collectionStatusFiftyPercent() {
    LogicalMeter districtHeatingMeter = createLogicalMeter(DISTRICT_HEATING_METER);

    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");
    PhysicalMeter physicalMeter = createPhysicalMeterWithInterval(
      districtHeatingMeter.id,
      30
    );

    addMeasurementsForMeterQuantities(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      start,
      1.0
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(
        "/meters"
        + "?after=2001-01-01T00:00:00.00Z"
        + "&before=2001-01-01T01:00:00.00Z",
        LogicalMeterDto.class
      ).getContent().get(0);

    assertThat(logicalMeterDto.collectionStatus).isEqualTo("50.0");
  }

  @Test
  public void collectionStatusTwoOutOfThreeMissing() {
    LogicalMeter districtHeatingMeter = createLogicalMeter(DISTRICT_HEATING_METER);

    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");
    PhysicalMeter physicalMeter = createPhysicalMeterWithInterval(
      districtHeatingMeter.id,
      15
    );

    addMeasurementsForMeterQuantities(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      start,
      1.0
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(
        "/meters"
        + "?after=2001-01-01T00:00:00.00Z"
        + "&before=2001-01-01T00:45:00.00Z",
        LogicalMeterDto.class
      ).getContent().get(0);

    assertThat(logicalMeterDto.collectionStatus).isEqualTo("33.33333333333333");
  }

  @Test
  public void collectionStatusOneHundredPercent() {
    LogicalMeter districtHeatingMeter = createLogicalMeter(DISTRICT_HEATING_METER);

    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T01:00:00.00Z");
    addMeasurementsForMeter(
      createPhysicalMeterWithInterval(
        districtHeatingMeter.id,
        60
      ),
      districtHeatingMeter.getQuantities(),
      start,
      Duration.ofDays(1),
      60L,
      1.0
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(
        "/meters"
        + "?after=2001-01-01T01:00:00.00Z"
        + "&before=2001-01-02T00:00:00.00Z",
        LogicalMeterDto.class
      ).getContent().get(0);

    assertThat(logicalMeterDto.collectionStatus).isEqualTo("100.0");
  }

  @Test
  public void findById() {
    LogicalMeter logicalMeter = createLogicalMeter();

    ResponseEntity<LogicalMeterDto> response = asTestUser()
      .get("/meters/" + logicalMeter.id, LogicalMeterDto.class);

    LogicalMeterDto logicalMeterDto = response.getBody();

    assertThatStatusIsOk(response);
    assertThat(logicalMeterDto.id).isEqualTo(logicalMeter.id);
  }

  @Test
  public void statusChangeLog() {
    LogicalMeter logicalMeter = createLogicalMeter();

    UUID physicalMeterId = randomUUID();
    physicalMeters.save(new PhysicalMeter(
      physicalMeterId,
      context().organisation(),
      "address",
      "external-id",
      "medium",
      "manufacturer",
      logicalMeter.id,
      0,
      0L,
      emptyList()
    ));
    StatusLogEntry<UUID> logEntry = createStatusLogForMeter(
      physicalMeterId,
      StatusType.OK,
      ZonedDateTime.parse("2001-01-01T10:14:00Z"),
      ZonedDateTime.parse("2001-01-06T10:14:00Z")
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .get("/meters/" + logicalMeter.id, LogicalMeterDto.class).getBody();

    assertThat(logicalMeterDto.statusChangelog).containsExactly(
      new MeterStatusLogDto(logEntry.id, "ok", "2001-01-01 10:14:00", "2001-01-06 10:14:00")
    );
  }

  @Test
  public void findAllPaged() {
    createLogicalMeter();
    createLogicalMeter();
    createLogicalMeter();

    Page<LogicalMeterDto> response = asTestUser()
      .getPage("/meters?size=1", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(3);
  }

  @Test
  public void findAllPagedSized() {
    createLogicalMeter();
    createLogicalMeter();
    createLogicalMeter();

    Page<LogicalMeterDto> response = asTestUser()
      .getPage("/meters?page=0&size=2", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(2);
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
  public void inactiveStatusesAreNotIncludedInStatusQueryForPeriod() {
    LogicalMeter firstLogicalMeter = createLogicalMeter();
    LogicalMeter secondLogicalMeter = createLogicalMeter();
    LogicalMeter thirdLogicalMeter = createLogicalMeter();

    PhysicalMeter firstMeter = createPhysicalMeter(firstLogicalMeter.id, "meter-one");
    PhysicalMeter secondMeter = createPhysicalMeter(secondLogicalMeter.id, "meter-two");
    PhysicalMeter thirdMeter = createPhysicalMeter(thirdLogicalMeter.id, "meter-three");

    // status is active within period, should be included
    createStatusLogForMeter(
      firstMeter.id,
      StatusType.ACTIVE,
      ZonedDateTime.parse("2004-12-25T10:14:00Z"),
      null
    );

    // status ended before period begun, should not be included
    createStatusLogForMeter(
      secondMeter.id,
      StatusType.ACTIVE,
      ZonedDateTime.parse("2002-12-25T10:14:00Z"),
      ZonedDateTime.parse("2004-09-14T12:12:12Z")
    );

    //status started after period ended, should not be included
    createStatusLogForMeter(
      thirdMeter.id,
      StatusType.ACTIVE,
      ZonedDateTime.parse("2015-04-29T10:14:20Z"),
      null
    );

    Page<LogicalMeterDto> response = asTestUser()
      .getPage(
        "/meters?after=2005-01-10T01:00:00.00Z"
        + "&before=2015-01-01T23:00:00.00Z"
        + "&status=active",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertThat(response.getContent().get(0).id)
      .isEqualTo(firstLogicalMeter.id);
  }

  @Test
  public void malformedDateParameter() {
    ResponseEntity<ErrorMessageDto> response = asTestUser()
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
    createLogicalMeter();
    createLogicalMeter(MeterDefinition.HOT_WATER_METER);

    Page<LogicalMeterDto> response = asTestUser()
      .getPage("/meters?medium=Hot water", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findsOwnOrganisationsMetersByFilter() {
    createLogicalMeter();

    Page<LogicalMeterDto> response = asTestUser()
      .getPage("/meters?organisation=" + context().getOrganisationId(), LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(1L);
  }

  @Test
  public void cannotAccessOtherOrganisationsMetersByFilter() {
    User user = userBuilder().build();
    createUserIfNotPresent(user);
    createLogicalMeter();

    Page<LogicalMeterDto> response = restClient()
      .loginWith(user.email, user.password)
      .tokenAuthorization()
      .getPage("/meters?organisation=" + context().getOrganisationId(), LogicalMeterDto.class);

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
      context().getOrganisationId(),
      hotWaterMeterDefinition,
      UNKNOWN_LOCATION
    ));

    Page<LogicalMeterDto> response = restClient()
      .loginWith("me@myorg.com", "secr3t")
      .tokenAuthorization()
      .getPage("/meters?medium=Hot water", LogicalMeterDto.class);

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

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get("/meters/" + theirMeter.id, ErrorMessageDto.class);

    assertThatStatusIsNotFound(response);
  }

  @Test
  public void meterNotFound() {
    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get("/meters/" + randomUUID(), ErrorMessageDto.class);

    assertThatStatusIsNotFound(response);
  }

  @Test
  public void findAllMapDataForLogicalMeters() {
    logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "my-mapped-meter",
      context().getOrganisationId(),
      MeterDefinition.UNKNOWN_METER,
      new Location(
        new GeoCoordinate(11.0, 22.0),
        "country",
        "city",
        "address"
      )
    ));
    ResponseEntity<List<MapMarkerDto>> response = asTestUser()
      .getList("/meters/map-markers", MapMarkerDto.class);

    assertThatStatusIsOk(response);
    assertThat(response.getBody().size()).isEqualTo(1);
  }

  @Test
  public void findAllMapDataForLogicalMetersWithParameters() {
    logicalMeterRepository.save(new LogicalMeter(
      randomUUID(),
      "my-mapped-meter",
      context().getOrganisationId(),
      MeterDefinition.UNKNOWN_METER,
      new Location(
        new GeoCoordinate(11.0, 22.0),
        "sweden",
        "varberg",
        "address"
      )
    ));
    ResponseEntity<List<MapMarkerDto>> response = asTestUser()
      .getList("/meters/map-markers?city=sweden,varberg", MapMarkerDto.class);

    assertThatStatusIsOk(response);
    assertThat(response.getBody().size()).isEqualTo(1);
  }

  @Test
  public void pagedLogicalMeterContainsLatestMeasurements() {
    LogicalMeter districtHeatingMeter = createLogicalMeter(DISTRICT_HEATING_METER);
    PhysicalMeter physicalMeter = createPhysicalMeter(districtHeatingMeter.id, "bowie");
    ZonedDateTime someDaysAgo = ZonedDateTime.now().minusDays(3);

    Set<Quantity> quantitiesWithoutDiffTemperature = new HashSet<>(asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.VOLUME_FLOW,
      Quantity.POWER,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE
    ));

    addMeasurementsForMeter(
      physicalMeter,
      quantitiesWithoutDiffTemperature,
      someDaysAgo,
      Duration.ofDays(1),
      60L,
      1.0
    );

    addMeasurementsForMeter(
      physicalMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now(),
      Duration.ofDays(1),
      60L,
      2.0
    );

    Page<LogicalMeterDto> page = asTestUser()
      .getPage("/meters?id=" + districtHeatingMeter.id, LogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
    LogicalMeterDto meter = page.getContent().get(0);

    List<MeasurementDto> measurements = meter.measurements;

    assertThat(measurements)
      .as("The difference temperature is missing")
      .hasSize(DISTRICT_HEATING_METER.quantities.size() - 1)
      .anyMatch(m -> m.quantity.equals(Quantity.ENERGY.name))
      .anyMatch(m -> m.quantity.equals(Quantity.VOLUME.name))
      .anyMatch(m -> m.quantity.equals(Quantity.POWER.name))
      .anyMatch(m -> m.quantity.equals(Quantity.FORWARD_TEMPERATURE.name))
      .anyMatch(m -> m.quantity.equals(Quantity.RETURN_TEMPERATURE.name))
      .noneMatch(m -> m.quantity.equals(Quantity.DIFFERENCE_TEMPERATURE.name));

    List<MeasurementDto> power = measurements
      .stream()
      .filter(m -> m.quantity.equals(Quantity.POWER.name))
      .collect(toList());

    assertThat(power)
      .as("Not showing duplicate values for a quantity")
      .hasSize(1);

    assertThat(power.get(0).value)
      .as("Only showing the latest value for a quantity")
      .isEqualTo(2.0);
  }

  @Test
  public void singleLogicalMeterContainsLatestMeasurements() {
    LogicalMeter districtHeatingMeter = createLogicalMeter(DISTRICT_HEATING_METER);
    PhysicalMeter physicalMeter = createPhysicalMeter(districtHeatingMeter.id, "bowie");
    ZonedDateTime someDaysAgo = ZonedDateTime.now().minusDays(3);

    Set<Quantity> quantitiesWithoutDiffTemperature = new HashSet<>(asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.VOLUME_FLOW,
      Quantity.POWER,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE
    ));

    addMeasurementsForMeter(
      physicalMeter,
      quantitiesWithoutDiffTemperature,
      someDaysAgo,
      Duration.ofDays(1),
      60L,
      1.0
    );

    addMeasurementsForMeter(
      physicalMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now(),
      Duration.ofDays(1),
      60L,
      2.0
    );

    ResponseEntity<LogicalMeterDto> response = asTestUser()
      .get("/meters/" + districtHeatingMeter.id, LogicalMeterDto.class);

    LogicalMeterDto meter = response.getBody();

    assertThatStatusIsOk(response);

    List<MeasurementDto> measurements = meter.measurements;

    assertThat(measurements)
      .as("The difference temperature is missing")
      .hasSize(DISTRICT_HEATING_METER.quantities.size() - 1)
      .anyMatch(m -> m.quantity.equals(Quantity.ENERGY.name))
      .anyMatch(m -> m.quantity.equals(Quantity.VOLUME.name))
      .anyMatch(m -> m.quantity.equals(Quantity.POWER.name))
      .anyMatch(m -> m.quantity.equals(Quantity.FORWARD_TEMPERATURE.name))
      .anyMatch(m -> m.quantity.equals(Quantity.RETURN_TEMPERATURE.name))
      .noneMatch(m -> m.quantity.equals(Quantity.DIFFERENCE_TEMPERATURE.name));

    List<MeasurementDto> power = measurements
      .stream()
      .filter(m -> m.quantity.equals(Quantity.POWER.name))
      .collect(toList());

    assertThat(power)
      .as("Not showing duplicate values for a quantity")
      .hasSize(1);

    assertThat(power.get(0).value)
      .as("Only showing the latest value for a quantity")
      .isEqualTo(2.0);
  }

  private void addMeasurementsForMeter(
    PhysicalMeter physicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime start,
    Duration periodDuration,
    Long minuteInterval,
    double value
  ) {
    ZonedDateTime now = start;
    while (now.isBefore(start.plus(periodDuration))) {
      addMeasurementsForMeterQuantities(physicalMeter, quantities, now, value);
      now = now.plusMinutes(minuteInterval);
    }
  }

  private void addMeasurementsForMeterQuantities(
    PhysicalMeter physicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime when,
    double value
  ) {
    for (Quantity quantity : quantities) {
      measurementUseCases.save(
        singletonList(new Measurement(
          null,
          when,
          quantity.name,
          value,
          quantity.presentationUnit(),
          physicalMeter
        ))
      );
    }
  }

  private LogicalMeter createLogicalMeter(MeterDefinition meterDefinition) {
    UUID meterId = UUID.randomUUID();
    return logicalMeterRepository.save(new LogicalMeter(
      meterId,
      meterId.toString(),
      context().getOrganisationId(),
      meterDefinition,
      Location.UNKNOWN_LOCATION
    ));
  }

  private LogicalMeter createLogicalMeter() {
    return createLogicalMeter(MeterDefinition.UNKNOWN_METER);
  }

  private StatusLogEntry<UUID> createStatusLogForMeter(
    UUID physicalMeterId,
    StatusType statusType,
    ZonedDateTime start,
    ZonedDateTime stop
  ) {
    return meterStatusLogs.save(new StatusLogEntry<>(
      null,
      physicalMeterId,
      statusType,
      start,
      stop
    ));
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
      .language(Language.en)
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
    Page<LogicalMeterDto> response = asTestUser()
      .getPage(url, LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);

    assertThat(actual.apply(response.getContent().get(0)))
      .as(errorMessage)
      .isEqualTo(expected);
  }

  private PhysicalMeter createPhysicalMeterWithInterval(UUID logicalMeterId, long interval) {
    UUID meterId = randomUUID();
    return physicalMeters.save(
      new PhysicalMeter(
        meterId,
        context().organisation(),
        "111-222-333-444-1",
        meterId.toString(),
        "Some device specific medium name",
        "ELV1",
        logicalMeterId,
        interval,
        null
      )
    );
  }

  private PhysicalMeter createPhysicalMeter(UUID logicalMeterId, String externalId) {
    return physicalMeters.save(
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
