package com.elvaco.mvp.bootstrap.demo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.elvaco.mvp.config.DemoData;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.repository.jpa.MeasurementRepository;
import com.elvaco.mvp.repository.jpa.MeteringPointRepository;
import com.elvaco.mvp.repository.jpa.OrganisationRepository;
import com.elvaco.mvp.repository.jpa.PhysicalMeterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
@DemoData
public class MeteringPointDatabaseLoader implements CommandLineRunner {

  private final MeteringPointRepository meteringPointRepository;
  private final MeasurementRepository measurementRepository;
  private final PhysicalMeterRepository physicalMeterRepository;
  private final OrganisationRepository organisationRepository;

  @Autowired
  public MeteringPointDatabaseLoader(MeteringPointRepository meteringPointRepository,
                                     MeasurementRepository measurementRepository,
                                     PhysicalMeterRepository physicalMeterRepository,
                                     OrganisationRepository organisationRepository) {
    this.meteringPointRepository = meteringPointRepository;
    this.measurementRepository = measurementRepository;
    this.physicalMeterRepository = physicalMeterRepository;
    this.organisationRepository = organisationRepository;

  }

  @Override
  public void run(String... args) {
    Random random = new Random();
    OrganisationEntity organisationEntity = new OrganisationEntity();
    organisationEntity.code = "elvaco";
    organisationEntity.name = "Elvaco AB";
    organisationRepository.save(organisationEntity);

    for (int i = 0; i < 10; ++i) {
      String meterIdentity = "DEMO-METER-" + i;

      // Metering points
      MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
      meteringPointEntity.propertyCollection = new PropertyCollection()
        .put("user", new UserPropertyDto(meterIdentity, "Demo project"))
        .putArray("numbers", asList(1, 2, 3, 17));
      meteringPointRepository.save(meteringPointEntity);

      // Physical meters
      PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
        organisationEntity,
        meterIdentity,
        "Electricity");
      physicalMeterEntity.meteringPoint = meteringPointEntity;
      physicalMeterRepository.save(physicalMeterEntity);

      // Measurements
      List<MeasurementEntity> measurementEntities = new ArrayList<>();
      for (int j = 0; j < 96; ++j) {
        MeasurementEntity measurementEntity = new MeasurementEntity(Date.from(Instant.now()),
          "Power",
          random.nextDouble(),
          "mW",
          physicalMeterEntity);
        measurementEntities.add(measurementEntity);
      }
      measurementRepository.save(measurementEntities);
    }
  }
}
