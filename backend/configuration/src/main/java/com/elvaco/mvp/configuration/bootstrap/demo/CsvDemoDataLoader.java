package com.elvaco.mvp.configuration.bootstrap.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.web.dto.GeoPositionDto;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simpleflatmapper.csv.CsvMapper;
import org.simpleflatmapper.csv.CsvMapperFactory;
import org.simpleflatmapper.csv.CsvParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

@Slf4j
@RequiredArgsConstructor
@Order(3)
@Profile("demo")
@Component
class CsvDemoDataLoader implements CommandLineRunner {

  private static final String DELIMITER = " :: ";

  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;
  private final Gateways gateways;
  private final SettingUseCases settingUseCases;
  private final StatusLogsDataLoader statusLogsDataLoader;
  private final Organisation rootOrganisation;
  private final MediumProvider mediumProvider;
  private final SystemMeterDefinitionProvider meterDefinitionProvider;
  private final AlarmDataLoader alarmDataLoader;

  private int daySeed = 1;

  @Transactional
  @Override
  public void run(String... args) throws Exception {
    if (settingUseCases.isDemoDataLoaded()) {
      log.info("Demo data seems to already be loaded - skipping demo data loading!");
      return;
    }

    log.info("Loading demo data from CSV");

    Map<String, Location> locationMap = mapAddressToLocation();

    importFrom("data/meters_fictive_hoganas.csv", locationMap);
    importFrom("data/meters_perstorp.csv", locationMap);
    importFrom("data/meters_almhult.csv", locationMap);

    long seed = 21914;
    Random random = new Random(seed);
    log.info("Creating mock demo data (seed: {})", seed);

    statusLogsDataLoader.load(random);
    alarmDataLoader.load(random);

    settingUseCases.setDemoDataLoaded();
  }

  private void importFrom(
    String filePath,
    Map<String, Location> locationMap
  ) throws IOException {
    AtomicInteger counter = new AtomicInteger(0);

    CsvParser.separator(';')
      .mapWith(csvMapper(MeterData.class))
      .stream(getFile(filePath), stream ->
        stream
          .map(csvData -> {
            LogicalMeter logicalMeter = LogicalMeter.builder()
              .externalId(csvData.facilityId)
              .organisationId(rootOrganisation.id)
              .meterDefinition(
                meterDefinitionProvider.getByMediumOrThrow(
                  mediumProvider.getByNameOrThrow(csvData.medium)
                )
              )
              .created(addDays())
              .location(locationMap.get(csvData.address.toLowerCase()))
              .utcOffset(csvData.utcOffset)
              .build();
            PhysicalMeter physicalMeter = PhysicalMeter.builder()
              .address(csvData.meterId)
              .externalId(csvData.facilityId)
              .medium(csvData.medium)
              .manufacturer(csvData.meterManufacturer)
              .organisationId(rootOrganisation.id)
              .readIntervalMinutes(counter.incrementAndGet() > 10 ? 1440 : 60)
              .mbusDeviceType((int) Math.floor(Math.random() * 10))
              .revision((int) Math.floor(Math.random() * 10))
              .activePeriod(PeriodRange.openFrom(ZonedDateTime.parse("2018-01-01T08:00:00Z"), null))
              .build();
            Gateway gateway = Gateway.builder()
              .organisationId(rootOrganisation.id)
              .serial(csvData.gatewayId)
              .productModel(csvData.gatewayProductModel)
              .build();
            return new Parameters(logicalMeter, physicalMeter, gateway);
          })
      )
      .forEach(p -> {
        gateways.save(p.gateway);
        logicalMeters.save(p.logicalMeter.toBuilder().gateway(p.gateway).build());
        physicalMeters.save(p.physicalMeter.toBuilder().logicalMeterId(p.logicalMeter.id).build());
      });
  }

  private ZonedDateTime addDays() {
    return ZonedDateTime.now().minusDays(daySeed++);
  }

  private static Map<String, Location> mapAddressToLocation() throws IOException {
    return loadGeodata().entrySet().stream()
      .map(entry -> parseKeyToLocation(entry.getKey())
        .coordinate(toGeoCoordinate(entry.getValue()))
        .build())
      .collect(toMap(Location::getAddress, Function.identity()));
  }

  private static Map<String, GeoPositionDto> loadGeodata() throws IOException {
    return OBJECT_MAPPER.readValue(
      getFile("data/geodata.json"),
      new TypeReference<Map<String, GeoPositionDto>>() {}
    );
  }

  private static LocationBuilder parseKeyToLocation(String s) {
    String[] parts = s.split(DELIMITER);
    return new LocationBuilder()
      .address(parts[0].trim())
      .city(parts[1].trim())
      .country(parts[2].trim());
  }

  private static <T> CsvMapper<T> csvMapper(Class<T> target) {
    return CsvMapperFactory.newInstance().newMapper(target);
  }

  private static URL loadResource(String file) {
    return CsvDemoDataLoader.class.getClassLoader().getResource(file);
  }

  private static File getFile(String filePath) {
    return new File(requireNonNull(loadResource(filePath)).getFile());
  }

  private static GeoCoordinate toGeoCoordinate(GeoPositionDto geoPosition) {
    return new GeoCoordinate(geoPosition.latitude, geoPosition.longitude, geoPosition.confidence);
  }

  @RequiredArgsConstructor
  private static class Parameters {

    private final LogicalMeter logicalMeter;
    private final PhysicalMeter physicalMeter;
    private final Gateway gateway;
  }
}
