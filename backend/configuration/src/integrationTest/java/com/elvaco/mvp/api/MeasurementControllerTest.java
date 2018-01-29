package com.elvaco.mvp.api;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.access.OrganisationMapper;
import com.elvaco.mvp.repository.access.UserMapper;
import com.elvaco.mvp.repository.jpa.MeasurementRepository;
import com.elvaco.mvp.repository.jpa.PhysicalMeterRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import static com.elvaco.mvp.fixture.DomainModels.DEVELOPER_USER;
import static com.elvaco.mvp.fixture.Entities.WAYNE_INDUSTRIES_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerTest extends IntegrationTest {

  @Autowired
  ModelMapper modelMapper;

  @Autowired
  private MeasurementRepository measurementRepository;

  @Autowired
  private PhysicalMeterRepository meterRepository;

  @Autowired
  private UserUseCases userUseCases;

  private Map<String, MeasurementEntity> measurementQuantities;

  @Before
  public void setUp() {
    UserMapper userMapper = new UserMapper(modelMapper, new OrganisationMapper());
    User elvacoUser = userUseCases.findByEmail("peteri@elvaco.se").get();
    User wayneIndustriesUser = userUseCases.findByEmail("user@wayne.se").get();

    PhysicalMeterEntity butterMeter =
      new PhysicalMeterEntity(
        userMapper.toEntity(elvacoUser).organisation,
        "test-butter-meter-1",
        "Butter"
      );

    PhysicalMeterEntity milkMeter =
      new PhysicalMeterEntity(
        userMapper.toEntity(wayneIndustriesUser).organisation,
        "test-milk-meter-1",
        "Milk"
      );

    meterRepository.save(Arrays.asList(butterMeter, milkMeter));

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
    ).map(measurementRepository::save).collect(Collectors.toMap(
      m -> m.quantity,
      Function.identity()
    ));
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

    assertThat(href).isEqualTo(restClient().getBaseUrl() + "/physical-meters/" + butterMeasurement.physicalMeter.id);
  }

  @Test
  public void canOnlySeeMeasurementsFromMeterBelongingToOrganisation() {
    Page<MeasurementDto> page = asElvacoUser().getPage("/measurements", MeasurementDto.class)
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

  private RestClient apiService() {
    return restClient().loginWith(DEVELOPER_USER.email, DEVELOPER_USER.password);
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
    Page<MeasurementDto> page = asSuperAdmin().getPage("/measurements", MeasurementDto.class)
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
