package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
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
import static java.util.Collections.singletonList;
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

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private MeterDefinitionJpaRepository meterDefinitionJpaRepository;

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
      newLogicalMeterEntity(
        "Butter",
        MeterDefinitionType.TEST_METER_TYPE_1,
        asList(new QuantityEntity(
          null,
          "Butter temperature",
          "°C"
        ), new QuantityEntity(
          null,
          "Left to walk",
          "m"
        ))
      ).id,
      15
    );

    wayneIndustriesEntity = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Wayne Industries",
        "wayne-industries",
        "wayne-industries"
      )
    );

    PhysicalMeterEntity milkMeter = new PhysicalMeterEntity(
      randomUUID(),
      wayneIndustriesEntity,
      "test-milk-meter-1",
      "milk-external-id",
      "Milk",
      "ELV",
      newLogicalMeterEntity(
        "Milk",
        MeterDefinitionType.TEST_METER_TYPE_2,
        singletonList(new QuantityEntity(
          null,
          "Milk temperature",
          "°C"
        ))
      ).id,
      15
    );

    forceMeter = new PhysicalMeterEntity(
      randomUUID(),
      wayneIndustriesEntity,
      String.valueOf(Math.random()),
      "force-external-id",
      "Vacuum",
      "ELV",
      newLogicalMeterEntity(
        "Vacuum",
        MeterDefinitionType.TEST_METER_TYPE_3,
        asList(new QuantityEntity(
          null,
          "Heat",
          "°C"
        ), new QuantityEntity(
          null,
          "LightsaberPower",
          "kW"
        ))
      ).id,
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
        "m",
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
    logicalMeterJpaRepository.deleteAll();
    meterDefinitionJpaRepository.delete(MeterDefinitionType.TEST_METER_TYPE_1);
    meterDefinitionJpaRepository.delete(MeterDefinitionType.TEST_METER_TYPE_2);
    meterDefinitionJpaRepository.delete(MeterDefinitionType.TEST_METER_TYPE_3);
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
      .getList("/measurements?quantities=Butter temperature:K", MeasurementDto.class)
      .getBody();

    assertThat(measurements.get(0).quantity).isEqualTo("Butter temperature");
    assertThat(measurements.get(0).unit).isEqualTo("K");
    assertThat(measurements.get(0).value).isEqualTo(285.59); // 12.44 Celsius = 285.59 Kelvin
  }

  @Test
  public void canOnlySeeMeasurementsFromMeterBelongingToOrganisation() {
    List<MeasurementDto> measurements = as(context().user)
      .getList("/measurements?quantities=Butter temperature:K", MeasurementDto.class)
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
    List<String> quantities =
      getListAsSuperAdmin("/measurements?meters=" + forceMeter.logicalMeterId)
        .stream()
        .map(c -> c.quantity)
        .collect(toList());

    assertThat(quantities).hasSize(2);
    assertThat(quantities).containsExactlyInAnyOrder("Heat", "LightsaberPower");
  }

  @Test
  public void fetchMeasurementsForHeatMeter() {
    List<MeasurementDto> contents = getListAsSuperAdmin("/measurements?quantities=Heat");

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
      getListAsSuperAdmin("/measurements?quantities=LightsaberPower&before=" + date);

    assertThat(contents).hasSize(1);
    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(0);
  }

  @Test
  public void fetchMeasurementsForMeterBeforeTime() {
    String date = "1990-01-01T08:00:00Z";
    List<MeasurementDto> contents =
      getListAsSuperAdmin("/measurements?before=" + date);

    assertThat(contents).hasSize(1);
    MeasurementDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(0);
  }

  @Test
  public void fetchMeasurementsForMeterAfterTime() {
    String date = "1990-01-01T08:00:00Z";
    List<String> foundQuantities =
      getListAsSuperAdmin("/measurements?after=" + date)
        .stream()
        .map(c -> c.quantity)
        .collect(toList());

    assertThat(foundQuantities).hasSize(4);
    assertThat(foundQuantities).doesNotContain("LightsaberPower");
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTime() {
    List<MeasurementDto> contents =
      getListAsSuperAdmin("/measurements?quantities=Heat&after=1990-01-01T08:00:00Z");

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Heat");
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTimeWithNonDefaultUnit() {
    List<MeasurementDto> contents =
      getListAsSuperAdmin("/measurements?quantities=Heat:K&after=1990-01-01T08:00:00Z");

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Heat");
    assertThat(dto.unit).isEqualTo("K");
    assertThat(dto.value).isEqualTo(423.15); // 150 degrees Celsius
  }

  @Test
  public void fetchMeasurementsForMeterUsingTwoDifferentQuantities() {
    List<MeasurementDto> contents =
      getListAsSuperAdmin(
        "/measurements?quantities=Heat:K,LightsaberPower:MW");

    assertThat(contents).hasSize(2);
    MeasurementDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Heat");
    assertThat(dto.unit).isEqualTo("K");
    assertThat(dto.value).isEqualTo(423.15); // 150 degrees Celsius

    dto = contents.get(1);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.unit).isEqualTo("MW");
    assertThat(dto.value).isEqualTo(0);
  }

  private LogicalMeterEntity newLogicalMeterEntity(
    String medium,
    MeterDefinitionType meterDefinitionType,
    List<QuantityEntity> quantityEntities
  ) {
    MeterDefinitionEntity meterDefinitionEntity = meterDefinitionJpaRepository.save(
      new MeterDefinitionEntity(
        meterDefinitionType,
        new HashSet<>(quantityEntities),
        medium,
        false
      ));
    UUID uuid = UUID.randomUUID();
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      UUID.randomUUID(),
      uuid.toString(),
      context().organisationEntity.id,
      ZonedDateTime.now(),
      meterDefinitionEntity
    ));

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
