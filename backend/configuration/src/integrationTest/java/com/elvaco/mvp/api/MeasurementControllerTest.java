package com.elvaco.mvp.api;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.jpa.MeasurementRepository;
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
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerTest extends IntegrationTest {

  @Autowired
  private MeasurementRepository measurementRepository;

  @Autowired
  private PhysicalMeterRepository meterRepository;

  private Map<String, MeasurementEntity> measurementQuantities;

  @Before
  public void setUp() {
    PhysicalMeterEntity butterMeter =
      new PhysicalMeterEntity(
        ELVACO_ENTITY,
        "test-butter-meter-1",
        "Butter"
      );

    PhysicalMeterEntity milkMeter =
      new PhysicalMeterEntity(
        WAYNE_INDUSTRIES_ENTITY,
        "test-milk-meter-1",
        "Milk"
      );

    meterRepository.save(asList(butterMeter, milkMeter));

    measurementQuantities = Stream.of(
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
      .map(measurementRepository::save)
      .collect(toMap(m -> m.quantity, Function.identity()));
  }

  @After
  public void tearDown() {
    meterRepository.deleteAll();
    measurementRepository.deleteAll();
  }

  @Test
  public void measurementsRetrievableAtEndpoint() {
    Page<MeasurementDto> page = asElvacoUser()
      .getPage("/measurements", MeasurementDto.class)
      .getBody()
      .newPage();

    assertThat(page.getContent().get(0).quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void measurementRetrievableById() {
    Long butterTemperatureId = idOf("Butter temperature");
    MeasurementDto measurement = asElvacoUser().get(
      "/measurements/" + butterTemperatureId,
      MeasurementDto.class
    )
      .getBody();

    assertThat(measurement.id).isEqualTo(butterTemperatureId);
    assertThat(measurement.quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void measurementUnitScaled() {
    Page<MeasurementDto> page = asElvacoUser()
      .getPage("/measurements?quantity=Butter temperature&scale=K", MeasurementDto.class)
      .getBody()
      .newPage();

    MeasurementDto measurementDto = page.getContent().get(0);
    assertThat(measurementDto.quantity).isEqualTo("Butter temperature");
    assertThat(measurementDto.unit).isEqualTo("K");
    assertThat(measurementDto.value).isEqualTo(285.59); // 12.44 Celsius = 285.59 Kelvin
  }

  @Test
  public void measurementLinksToItsPhysicalMeter() {
    MeasurementEntity butterMeasurement = measurementOf("Butter temperature");
    String href = asElvacoUser()
      .get("/measurements/" + butterMeasurement.id, MeasurementDto.class)
      .getBody()
      .physicalMeter
      .getHref();

    Long physicalMeterId = butterMeasurement.physicalMeter.id;
    assertThat(href).isEqualTo(restClient().getBaseUrl() + "/physical-meters/" + physicalMeterId);
  }

  @Test
  public void canOnlySeeMeasurementsFromMeterBelongingToOrganisation() {
    Page<MeasurementDto> page = asElvacoUser()
      .getPage("/measurements", MeasurementDto.class)
      .getBody()
      .newPage();
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
    Page<MeasurementDto> page = asSuperAdmin()
      .getPage("/measurements", MeasurementDto.class)
      .getBody()
      .newPage();
    assertThat(page).hasSize(3);
  }

  private Long idOf(String measurementQuantity) {
    return measurementOf(measurementQuantity).id;
  }

  private MeasurementEntity measurementOf(String measurementQuantity) {
    return measurementQuantities.get(measurementQuantity);
  }
}
