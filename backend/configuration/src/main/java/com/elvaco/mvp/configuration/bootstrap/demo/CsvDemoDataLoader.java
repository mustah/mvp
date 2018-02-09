package com.elvaco.mvp.configuration.bootstrap.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayRepository;
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
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Order(3)
@Profile("demo")
@Component
public class CsvDemoDataLoader implements CommandLineRunner {

  private static final String DELIMITER = " :: ";

  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;
  private final GatewayRepository gatewayRepository;
  private final SettingUseCases settingUseCases;

  @Autowired
  public CsvDemoDataLoader(
    LogicalMeters logicalMeters,
    PhysicalMeters physicalMeters,
    GatewayRepository gatewayRepository,
    SettingUseCases settingUseCases
  ) {
    this.logicalMeters = logicalMeters;
    this.physicalMeters = physicalMeters;
    this.gatewayRepository = gatewayRepository;
    this.settingUseCases = settingUseCases;
  }

  @Override
  public void run(String... args) throws Exception {
    if (settingUseCases.isDemoDataLoaded()) {
      log.info("Demo data seems to already be loaded - skipping demo data loading!");
      return;
    }

    importFrom("data/meters_perstorp.csv");

    settingUseCases.setDemoDataLoaded();
  }

  private void importFrom(String filePath) throws IOException {
    Map<String, Location> locationMap = mapAddressToLocation();
    CsvParser.separator(';')
      .mapWith(csvMapper(MeterData.class))
      .stream(getFile(filePath), stream ->
        stream
          .map(csvData -> {
            LogicalMeter logicalMeter = new LogicalMeter(
              null,
              csvData.meterStatus,
              locationMap.get(csvData.address),
              new Date(),
              new PropertyCollection(new UserProperty(csvData.facilityId))
            );
            PhysicalMeter physicalMeter = new PhysicalMeter(
              ELVACO,
              csvData.meterId,
              csvData.medium,
              csvData.meterManufacturer
            );
            GatewayEntity gateway = new GatewayEntity(
              csvData.gatewayId,
              csvData.gatewayProductModel
            );
            return new Parameters(logicalMeter, physicalMeter, gateway);
          })
      )
      .forEach(p -> {
        gatewayRepository.save(p.gateway);
        LogicalMeter logicalMeter = logicalMeters.save(p.logicalMeter);
        PhysicalMeter physicalMeter = p.physicalMeter;
        physicalMeters.save(
          new PhysicalMeter(
            physicalMeter.id,
            physicalMeter.organisation,
            physicalMeter.identity,
            physicalMeter.medium,
            physicalMeter.manufacturer,
            logicalMeter.id));
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
      .streetAddress(parts[0])
      .city(parts[1])
      .country(parts[2]);
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
    private final GatewayEntity gateway;

    private Parameters(
      LogicalMeter logicalMeter,
      PhysicalMeter physicalMeter,
      GatewayEntity gateway
    ) {
      this.logicalMeter = logicalMeter;
      this.physicalMeter = physicalMeter;
      this.gateway = gateway;
    }
  }
}
