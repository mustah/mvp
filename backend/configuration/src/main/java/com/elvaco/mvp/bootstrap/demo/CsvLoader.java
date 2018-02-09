package com.elvaco.mvp.bootstrap.demo;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.ToString;
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

@Order(3)
@Profile("demo")
@Component
public class CsvLoader implements CommandLineRunner {

  private static final String DELIMITER = " :: ";

  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;

  @Autowired
  public CsvLoader(LogicalMeters logicalMeters, PhysicalMeters physicalMeters) {
    this.logicalMeters = logicalMeters;
    this.physicalMeters = physicalMeters;
  }

  @Override
  public void run(String... args) throws Exception {
    testing();
  }

  private void testing() throws java.io.IOException {
    URL metersResource = loadResource("data/meters_perstorp.csv");
    URL geoDataResource = loadResource("data/geodata.json");

    Map<String, GeoPositionDto> map =
      OBJECT_MAPPER.readValue(
        getFile(geoDataResource),
        new TypeReference<Map<String, GeoPositionDto>>() {}
      );

    Map<String, Location> locationMap = map.entrySet()
      .stream()
      .map(entry -> parseKeyToLocation(entry.getKey())
        .coordinate(toGeoCoordinate(entry.getValue()))
        .build())
      .collect(toMap(location -> location.getStreetAddress().get(), Function.identity()));

    CsvParser.separator(';')
      .mapWith(csvMapper())
      .stream(getFile(metersResource), stream ->
        stream
          .map(meterData -> {
            LogicalMeter logicalMeter = logicalMeters.save(new LogicalMeter(
              1L,
              meterData.meterStatus,
              locationMap.get(meterData.address),
              new Date(),
              new PropertyCollection(new UserProperty(meterData.facilityId))
            ));
            return new PhysicalMeter(
              ELVACO,
              meterData.meterId,
              meterData.medium,
              meterData.meterManufacturer,
              logicalMeter.id
            );
          })
      )
      .peek(physicalMeters::save)
      .forEach(System.out::println);
  }

  private static LocationBuilder parseKeyToLocation(String s) {
    String[] parts = s.split(DELIMITER);
    return new LocationBuilder()
      .streetAddress(parts[0])
      .city(parts[1])
      .country(parts[2]);
  }

  private static CsvMapper<MeterData> csvMapper() {
    return CsvMapperFactory.newInstance().newMapper(MeterData.class);
  }

  private static URL loadResource(String file) {
    return CsvLoader.class.getClassLoader().getResource(file);
  }

  private static File getFile(URL metersResource) {
    return new File(requireNonNull(metersResource).getFile());
  }

  private static GeoCoordinate toGeoCoordinate(GeoPositionDto geoPosition) {
    return new GeoCoordinate(geoPosition.latitude, geoPosition.longitude, geoPosition.confidence);
  }

  @Nullable
  private static String toNull(String value) {
    return value.equals("NULL") ? null : value;
  }

  @ToString
  public static class MeterData {

    public final String meterId;
    public final String meterStatus;
    public final String facilityId;
    public final String address;
    public final String city;
    public final String medium;
    public final String meterManufacturer;
    public final String phone;
    @Nullable
    public final String ip;
    @Nullable
    public final String port;

    public final Long gatewayId;
    public final String gatewayProductModel;
    public final String gatewayStatus;

    public MeterData(
      String facilityId,
      String address,
      String city,
      String medium,
      String meterId,
      String meterManufacturer,
      Long gatewayId,
      String gatewayProductModel,
      String phone,
      String ip,
      String port,
      String meterStatus,
      String gatewayStatus
    ) {
      this.facilityId = facilityId;
      this.address = address;
      this.city = city;
      this.meterId = meterId;
      this.medium = medium;
      this.meterManufacturer = meterManufacturer;
      this.gatewayId = gatewayId;
      this.gatewayProductModel = gatewayProductModel;
      this.phone = phone;
      this.ip = toNull(ip);
      this.port = toNull(port);
      this.meterStatus = meterStatus;
      this.gatewayStatus = gatewayStatus;
    }
  }
}
