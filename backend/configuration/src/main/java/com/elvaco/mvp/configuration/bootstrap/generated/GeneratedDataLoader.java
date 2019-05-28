package com.elvaco.mvp.configuration.bootstrap.generated;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.generator.GeneratedData;
import com.elvaco.mvp.generator.MeterPopulationSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

@Slf4j
@Order(3)
@RequiredArgsConstructor
@Profile("generate")
@Component
class GeneratedDataLoader implements CommandLineRunner {

  private final PhysicalMeters physicalMeters;
  private final LogicalMeters logicalMeters;
  private final Gateways gateways;
  private final Organisations organisations;
  private final MeasurementUseCases measurementUseCases;
  private final MediumProvider mediumProvider;
  private final SystemMeterDefinitionProvider meterDefinitionProvider;

  @Override
  public void run(String... args) {
    UUID id = UUID.randomUUID();
    Organisation organisation = organisations.saveAndFlush(Organisation.of(id.toString(), id));

    log.info("Generating data for organisation '{}'...", organisation);
    long start = System.nanoTime();
    ZonedDateTime now = ZonedDateTime.now();
    MeterPopulationSpecification specification = new MeterPopulationSpecification(1L)
      .withDefinitionsFrom(
        singleton(meterDefinitionProvider.getByMediumOrThrow(
          mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
        )
      )
      .withMeterCount(10)
      .withMeasurementLossFactor(0.0001)
      .withMeasurementsBetween(now.minusMonths(2), now)
      .withOrganisation(organisation)
      .withReportInterval(Duration.ofHours(1));

    List<GeneratedData> data = specification.create();

    log.info(
      "Data generated in {} seconds.",
      Duration.ofNanos(System.nanoTime() - start).getSeconds()
    );

    List<Measurement> allMeasurements = data.stream()
      .flatMap(gd -> gd.measurements.stream())
      .collect(toList());

    start = System.nanoTime();
    log.info("Saving generated data ...");
    saveReferenceInfo(
      data.stream().map(gd -> gd.gateway).collect(toList()),
      data.stream().map(gd -> gd.logicalMeter).collect(toList()),
      data.stream().map(gd -> gd.physicalMeter).collect(toList())
    );
    log.info(
      "Saved generated reference info data in {} seconds",
      Duration.ofNanos(System.nanoTime() - start).getSeconds()
    );
    saveMeasurements(allMeasurements);
  }

  @Transactional
  void saveReferenceInfo(
    List<Gateway> gatewayList,
    List<LogicalMeter> logicalMeterList,
    List<PhysicalMeter> physicalMeterList
  ) {
    gatewayList.forEach(gateways::save);
    logicalMeterList.forEach(logicalMeters::save);
    physicalMeterList.forEach(physicalMeters::save);
  }

  private void saveMeasurements(List<Measurement> allMeasurements) {
    int batchSz = 20000;
    long allStart = System.nanoTime();
    for (int i = 0; i < allMeasurements.size(); i += batchSz) {
      long start = System.nanoTime();
      int sz = Math.min(batchSz, allMeasurements.size() - i);
      allMeasurements.subList(i, i + sz).forEach( (measurement) ->
        measurementUseCases.createOrUpdate(measurement,
          logicalMeters.findById(measurement.physicalMeter.logicalMeterId).orElseThrow()));
      log.info(
        "Saved {} measurements ({}/{} total) in {} seconds ({}s total)",
        sz,
        i + sz,
        allMeasurements.size(),
        Duration.ofNanos(System.nanoTime() - start).getSeconds(),
        Duration.ofNanos(System.nanoTime() - allStart).getSeconds()
      );
    }
  }
}
