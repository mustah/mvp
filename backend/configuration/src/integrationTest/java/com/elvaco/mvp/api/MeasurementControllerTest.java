package com.elvaco.mvp.api;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.PhysicalMeterRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import static com.elvaco.mvp.fixture.Entities.ELVACO_ENTITY;
import static com.elvaco.mvp.fixture.Entities.WAYNE_INDUSTRIES_ENTITY;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerTest extends IntegrationTest {

  @Autowired
  private MeasurementJpaRepository measurementJpaRepository;

  @Autowired
  private PhysicalMeterRepository physicalMeterRepository;

  private Map<String, MeasurementEntity> measurementQuantities;
  private PhysicalMeterEntity forceMeter;

  @Before
  public void setUp() {
    PhysicalMeterEntity butterMeter = new PhysicalMeterEntity(
      ELVACO_ENTITY,
      "test-butter-meter-1",
      "Butter"
    );
    PhysicalMeterEntity milkMeter = new PhysicalMeterEntity(
      WAYNE_INDUSTRIES_ENTITY,
      "test-milk-meter-1",
      "Milk"
    );
    forceMeter = new PhysicalMeterEntity(
      WAYNE_INDUSTRIES_ENTITY,
      String.valueOf(Math.random()),
      "vacum"
    );

    physicalMeterRepository.save(asList(butterMeter, milkMeter, forceMeter));

    // What are midichlorians measured in?
    // https://scifi.stackexchange.com/a/28354
    measurementQuantities = Stream.of(
      new MeasurementEntity(
        new Date(),
        "Heat",
        150,
        "°C",
        forceMeter
      ),
      new MeasurementEntity(
        Date.from(Instant.parse("1983-05-24T12:00:01Z")),
        "LightsaberPower",
        0,
        "kW",
        forceMeter
      ),
      new MeasurementEntity(
        new Date(),
        "Butter temperature",
        12.44,
        "°C",
        butterMeter
      ),
      new MeasurementEntity(
        new Date(),
        "Left to walk",
        500,
        "mi",
        butterMeter
      ),
      new MeasurementEntity(
        new Date(),
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
  }

  @Test
  public void measurementsRetrievableAtEndpoint() {
    Page<MeasurementDto> page = asElvacoUser()
      .getPage("/measurements", MeasurementDto.class);

    assertThat(page.getContent().get(0).quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void measurementRetrievableById() {
    Long butterTemperatureId = idOf("Butter temperature");
    MeasurementDto measurement = asElvacoUser()
      .get("/measurements/" + butterTemperatureId, MeasurementDto.class)
      .getBody();

    assertThat(measurement.id).isEqualTo(butterTemperatureId);
    assertThat(measurement.quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void measurementUnitScaled() {
    Page<MeasurementDto> page = asElvacoUser()
      .getPage("/measurements?quantity=Butter temperature&scale=K", MeasurementDto.class);

    MeasurementDto measurementDto = page.getContent().get(0);
    assertThat(measurementDto.quantity).isEqualTo("Butter temperature");
    assertThat(measurementDto.unit).isEqualTo("K");
    assertThat(measurementDto.value).isEqualTo(285.59); // 12.44 Celsius = 285.59 Kelvin
  }

  /*@Test
  public void measurementLinksToItsPhysicalMeter() {
    MeasurementEntity butterMeasurement = measurementOf("Butter temperature");
    String href = asElvacoUser()
      .get("/measurements/" + butterMeasurement.id, MeasurementDto.class)
      .getBody()
      .physicalMeter
      .getHref();

    Long physicalMeterId = butterMeasurement.physicalMeter.id;
    assertThat(href).isEqualTo(restClient().getBaseUrl() + "/physical-meters/" + physicalMeterId);
  }*/

  @Test
  public void canOnlySeeMeasurementsFromMeterBelongingToOrganisation() {
    Page<MeasurementDto> page = asElvacoUser()
      .getPage("/measurements", MeasurementDto.class);

    page.forEach(
      measurementDto -> assertThat(measurementDto.quantity).isNotEqualTo("Milk temperature")
    );
  }

  @Test
  public void cannotAccessMeasurementIdOfOtherOrganisationDirectly() {
    HttpStatus statusCode = asElvacoUser()
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
    assertThat(getPageAsSuperAdmin("/measurements")).hasSize(5);
  }

  @Test
  public void fetchMeasurementsForMeter() {
    List<String> quantities = getPageAsSuperAdmin("/measurements?meterId=" + forceMeter.id)
      .getContent()
      .stream()
      .map(c -> c.quantity)
      .collect(toList());

    assertThat(quantities).hasSize(2);
    assertThat(quantities).containsExactlyInAnyOrder("Heat", "LightsaberPower");
  }

  @Test
  public void fetchMeasurementsForHeatMeter() {
    List<MeasurementDto> contents = getPageAsSuperAdmin("/measurements?quantity=Heat").getContent();

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Heat");

    // TODO[!must!] fix this when we have a PhysicalModelMapper in place!
    /*String valueAndUnit = dto.value + " " + dto.unit;
    assertThat(toMeasurementUnit(valueAndUnit, "K").toString()).isEqualTo("423.15 K");*/
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityBeforeTime() {
    String date = "1990-01-01T08:00:00Z";
    List<MeasurementDto> contents =
      getPageAsSuperAdmin("/measurements?quantity?LightsaberPower&before=" + date)
        .getContent();

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(0);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTime() {
    List<MeasurementDto> contents =
      getPageAsSuperAdmin("/measurements?quantity=Heat&after=1990-01-01T08:00:00Z")
        .getContent();

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Heat");
  }

  private Page<MeasurementDto> getPageAsSuperAdmin(String url) {
    return asSuperAdmin().getPage(url, MeasurementDto.class);
  }

  private Long idOf(String measurementQuantity) {
    return measurementOf(measurementQuantity).id;
  }

  private MeasurementEntity measurementOf(String measurementQuantity) {
    return measurementQuantities.get(measurementQuantity);
  }
}
