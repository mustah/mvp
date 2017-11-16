package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.MeasurementDTO;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import com.elvaco.mvp.repository.PhysicalMeterRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;
import com.elvaco.mvp.testdata.RestResponsePage;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerTest extends IntegrationTest {
  @Autowired
  private MeasurementRepository repository;
  @Autowired
  private PhysicalMeterRepository meterRepository;

  @Before
  public void setUp() throws Exception {
    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(0L /*fixme: this should be an organisation entity*/,
      "test-butter-meter-1",
      "Butter");
    meterRepository.save(physicalMeterEntity);
    Stream.of(
      new MeasurementEntity(
        new Date(),
        "Butter temperature",
        12.44,
        "°C",
        physicalMeterEntity),
      new MeasurementEntity(
        new Date(),
        "Left to walk",
        500,
        "mi",
        physicalMeterEntity)
    ).forEach(repository::save);
  }

  private RestClient rest() {
    return restClient().loginWith("user", "password");
  }

  @Test
  public void MeasurementsRetrievableAtEndpoint() {
    ResponseEntity<RestResponsePage<MeasurementDTO>> responseEntity = rest().getPage("/measurements", MeasurementDTO.class);

    RestResponsePage<MeasurementDTO> measurementsPage = responseEntity.getBody();
    Page<MeasurementDTO> page = measurementsPage.pageImpl();
    List<MeasurementDTO> contents = page.getContent();
    assertThat(contents.get(0).quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void MeasurementRetrievableById() {
    MeasurementDTO measurement = rest().get("/measurements/1", MeasurementDTO.class).getBody();
    assertThat(measurement.id).isEqualTo(1L);
    assertThat(measurement.quantity).isEqualTo("Butter temperature");
    assertThat(measurement.value).isEqualTo(12.44);
  }

  @Test
  public void MeasurementLinksToItsPhysicalMeter() {
    MeasurementDTO measurement = rest().get("/measurements/1", MeasurementDTO.class).getBody();
    assertThat(measurement.physicalMeter.getHref()).isEqualTo(restClient().getBaseURL() + "/physical-meters/1");
  }
}
