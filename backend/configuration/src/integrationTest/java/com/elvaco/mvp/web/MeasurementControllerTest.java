package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeasurementDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerTest extends IntegrationTest {

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterRepository;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  private Map<String, MeasurementEntity> measurementQuantities;
  private PhysicalMeterEntity forceMeter;
  private OrganisationEntity wayneIndustriesEntity;

  @Before
  public void setUp() {

    PhysicalMeterEntity butterMeter = new PhysicalMeterEntity(
      randomUUID(),
      context().organisationEntity,
      "test-butter-meter-1",
      "butter-external-id",
      "Butter",
      "ELV",
      null,
      15
    );

    wayneIndustriesEntity = organisationJpaRepository.save(
      new OrganisationEntity(randomUUID(), "Wayne Industries", "wayne-industries")
    );
    PhysicalMeterEntity milkMeter = new PhysicalMeterEntity(
      randomUUID(),
      wayneIndustriesEntity,
      "test-milk-meter-1",
      "milk-external-id",
      "Milk",
      "ELV",
      null,
      15
    );
    forceMeter = new PhysicalMeterEntity(
      randomUUID(),
      wayneIndustriesEntity,
      String.valueOf(Math.random()),
      "force-external-id",
      "vacum",
      "ELV",
      null,
      15
    );

    physicalMeterRepository.save(asList(butterMeter, milkMeter, forceMeter));

    // What are midichlorians measured in?
    // https://scifi.stackexchange.com/a/28354
    measurementQuantities = Stream.of(
      new MeasurementEntity(
        ZonedDateTime.now(),
        "Heat",
        150,
        "°C",
        forceMeter
      ),
      new MeasurementEntity(
        ZonedDateTime.parse("1983-05-24T12:00:01Z"),
        "LightsaberPower",
        0,
        "kW",
        forceMeter
      ),
      new MeasurementEntity(
        ZonedDateTime.now(),
        "Butter temperature",
        12.44,
        "°C",
        butterMeter
      ),
      new MeasurementEntity(
        ZonedDateTime.now(),
        "Left to walk",
        500,
        "mi",
        butterMeter
      ),
      new MeasurementEntity(
        ZonedDateTime.now(),
        "Milk temperature",
        7.1,
        "°C",
        milkMeter
      )
    )
      .map(measurementJpaRepository::save)
      .collect(toMap(m -> m.quantity, Function.identity()));
  }

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    physicalMeterRepository.deleteAll();
    organisationJpaRepository.delete(wayneIndustriesEntity);
  }

  @Test
  public void measurementsRetrievableAtEndpoint() {
    List<MeasurementDto> measurements = as(context().user)
      .getList("/measurements", MeasurementDto.class).getBody();

    List<String> quantities = measurements.stream()
      .map(m -> m.quantity)
      .collect(toList());

    assertThat(quantities).contains("Butter temperature");
  }

  @Test
  public void measurementRetrievableById() {
    Long butterTemperatureId = idOf("Butter temperature");
    MeasurementDto measurement = as(context().user)
      .get("/measurements/" + butterTemperatureId, MeasurementDto.class)
      .getBody();

    assertThat(measurement.id).isEqualTo(butterTemperatureId);
    assertThat(measurement.quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void measurementUnitScaled() {
    List<MeasurementDto> measurements = as(context().user)
      .getList("/measurements?quantity=Butter temperature&scale=K", MeasurementDto.class)
      .getBody();

    assertThat(measurements.get(0).quantity).isEqualTo("Butter temperature");
    assertThat(measurements.get(0).unit).isEqualTo("K");
    assertThat(measurements.get(0).value).isEqualTo(285.59); // 12.44 Celsius = 285.59 Kelvin
  }

  @Test
  public void canOnlySeeMeasurementsFromMeterBelongingToOrganisation() {
    List<MeasurementDto> measurements = as(context().user)
      .getList("/measurements?quantity=Butter temperature&scale=K", MeasurementDto.class)
      .getBody();

    List<String> names = measurements.stream().map(m -> m.quantity).collect(toList());

    assertThat(names).doesNotContain("Elvco");
  }

  @Test
  public void cannotAccessMeasurementIdOfOtherOrganisationDirectly() {
    HttpStatus statusCode = as(context().user)
      .get("/measurements/" + idOf("Milk temperature"), MeasurementDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void superAdminCanAccessAnyMeasurementDirectly() {
    HttpStatus statusCode = asSuperAdmin()
      .get("/measurements/" + idOf("Milk temperature"), MeasurementDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);

    statusCode = asSuperAdmin()
      .get("/measurements/" + idOf("Butter temperature"), MeasurementDto.class)
      .getStatusCode();

    assertThat(statusCode).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void superAdminCanSeeAllMeasurements() {
    assertThat(getListAsSuperAdmin("/measurements")).hasSize(5);
  }

  @Test
  public void fetchMeasurementsForMeter() {
    List<String> quantities = getListAsSuperAdmin("/measurements?meterId=" + forceMeter.id)
      .stream()
      .map(c -> c.quantity)
      .collect(toList());

    assertThat(quantities).hasSize(2);
    assertThat(quantities).containsExactlyInAnyOrder("Heat", "LightsaberPower");
  }

  @Test
  public void fetchMeasurementsForHeatMeter() {
    List<MeasurementDto> contents = getListAsSuperAdmin("/measurements?quantity=Heat");

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Heat");

    // TODO[!must!] fix this when we have a PhysicalMeterMapper in place!
    /*String valueAndUnit = dto.value + " " + dto.unit;
    assertThat(toMeasurementUnit(valueAndUnit, "K").toString()).isEqualTo("423.15 K");*/
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityBeforeTime() {
    String date = "1990-01-01T08:00:00Z";
    List<MeasurementDto> contents =
      getListAsSuperAdmin("/measurements?quantity=LightsaberPower&before=" + date);

    assertThat(contents).hasSize(1);
    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(0);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTime() {
    List<MeasurementDto> contents =
      getListAsSuperAdmin("/measurements?quantity=Heat&after=1990-01-01T08:00:00Z");

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Heat");
  }

  private List<MeasurementDto> getListAsSuperAdmin(String url) {
    return asSuperAdmin().getList(url, MeasurementDto.class).getBody();
  }

  private Long idOf(String measurementQuantity) {
    return measurementOf(measurementQuantity).id;
  }

  private MeasurementEntity measurementOf(String measurementQuantity) {
    return measurementQuantities.get(measurementQuantity);
  }

}
