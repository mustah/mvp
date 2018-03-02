package com.elvaco.mvp.configuration.bootstrap.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.simpleflatmapper.csv.CsvMapper;
import org.simpleflatmapper.csv.CsvMapperFactory;
import org.simpleflatmapper.csv.CsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.database.util.Json.OBJECT_MAPPER;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Order(3)
@Profile("demo")
@Component
public class CsvDemoDataLoader implements CommandLineRunner {

  private static final String DELIMITER = " :: ";

  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;
  private final MeterDefinitions meterDefinitions;
  private final SettingUseCases settingUseCases;
  private final Gateways gateways;

  @Autowired
  public CsvDemoDataLoader(
    LogicalMeters logicalMeters,
    PhysicalMeters physicalMeters,
    MeterDefinitions meterDefinitions,
    SettingUseCases settingUseCases,
    Gateways gateways
  ) {
    this.logicalMeters = logicalMeters;
    this.physicalMeters = physicalMeters;
    this.gateways = gateways;
    this.meterDefinitions = meterDefinitions;
    this.settingUseCases = settingUseCases;
  }

  @Override
  public void run(String... args) throws Exception {
    if (settingUseCases.isDemoDataLoaded()) {
      log.info("Demo data seems to already be loaded - skipping demo data loading!");
      return;
    }

    MeterDefinition meterDefinition = meterDefinitions.save(MeterDefinition.DISTRICT_HEATING_METER);

    Map<String, Location> locationMap = mapAddressToLocation();

    importFrom("data/meters_perstorp.csv", locationMap, meterDefinition);
    importFrom("data/meters_almhult.csv", locationMap, meterDefinition);

    settingUseCases.setDemoDataLoaded();
  }

  private void importFrom(
    String filePath,
    Map<String, Location> locationMap,
    MeterDefinition meterDefinition
  ) throws IOException {
    CsvParser.separator(';')
      .mapWith(csvMapper(MeterData.class))
      .stream(getFile(filePath), stream ->
        stream
          .map(csvData -> {
            Gateway gateway = new Gateway(
              null,
              ELVACO.id,
              csvData.gatewayId,
              csvData.gatewayProductModel
            );
            LogicalMeter logicalMeter = new LogicalMeter(
              randomUUID(),
              csvData.facilityId,
              ELVACO.id,
              locationMap.get(csvData.address),
              new Date(),
              emptyList(),
              meterDefinition,
              emptyList(),
              emptyList()
            );
            PhysicalMeter physicalMeter = new PhysicalMeter(
              randomUUID(),
              csvData.meterId,
              csvData.facilityId,
              csvData.medium,
              csvData.meterManufacturer,
              ELVACO
            );

            return new Parameters(logicalMeter, physicalMeter, gateway);
          })
      )
      .forEach(p -> {
        Gateway gateway = gateways.save(p.gateway);
        LogicalMeter logicalMeter = logicalMeters.save(p.logicalMeter.withGateway(gateway));
        PhysicalMeter physicalMeter = p.physicalMeter;
        physicalMeters.save(
          new PhysicalMeter(
            physicalMeter.id,
            physicalMeter.organisation,
            physicalMeter.address,
            physicalMeter.externalId,
            physicalMeter.medium,
            physicalMeter.manufacturer,
            logicalMeter.id,
            emptyList()
          ));
      });
  }

  private static Map<String, Location> mapAddressToLocation() throws IOException {
    return loadGeodata()
      .entrySet()
      .stream()
      .map(entry -> parseKeyToLocation(entry.getKey())
        .coordinate(toGeoCoordinate(entry.getValue()))
        .build())
      .collect(toMap(location -> location.getStreetAddress().get(), Function.identity()));
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
      .streetAddress(parts[0].trim())
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

  private static class Parameters {

    private final LogicalMeter logicalMeter;
    private final PhysicalMeter physicalMeter;
    private final Gateway gateway;

    private Parameters(
      LogicalMeter logicalMeter,
      PhysicalMeter physicalMeter,
      Gateway gateway
    ) {
      this.logicalMeter = logicalMeter;
      this.physicalMeter = physicalMeter;
      this.gateway = gateway;
    }
  }
}
