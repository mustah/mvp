package com.elvaco.mvp.web.mapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MapMarkersDto;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.StatusType.ACTIVE;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MapMarkersDtoMapperTest {

  @Test
  public void emptyMapMarkersFromEmptyGatewayList() {
    List<Gateway> gateways = emptyList();
    MapMarkersDto mapMarkersDto = MapMarkersDtoMapper.fromGateways(gateways);

    assertThat(mapMarkersDto.markers).isEmpty();
  }

  @Test
  public void emptyMapMarkersFromEmptyLogicalMeterList() {
    List<LogicalMeter> meters = emptyList();
    MapMarkersDto mapMarkersDto = MapMarkersDtoMapper.fromLogicalMeters(meters);

    assertThat(mapMarkersDto.markers).isEmpty();
  }

  @Test
  public void mapSingleGatewayByStatusType() {
    Gateway gateway = Gateway.builder()
      .meter(logicalMeterWith(kungsbacka()))
      .statusLog(statusLogEntry(ACTIVE))
      .build();

    MapMarkersDto mapMarkersDto = MapMarkersDtoMapper.fromGateways(singletonList(gateway));

    assertThat(mapMarkersDto.markers).isEqualTo(ImmutableMultimap.builder()
      .put(ACTIVE.name, new MapMarkerDto(gateway.id, 1.222, 2.111, ACTIVE))
      .build()
      .asMap());
  }

  @Test
  public void mapSingleLogicalMeterByStatusType() {
    LogicalMeter logicalMeter = logicalMeterWith(kungsbacka())
      .withPhysicalMeter(PhysicalMeter.builder()
        .status(statusLogEntry(ACTIVE))
        .build());

    MapMarkersDto mapMarkersDto =
      MapMarkersDtoMapper.fromLogicalMeters(singletonList(logicalMeter));

    assertThat(mapMarkersDto.markers).isEqualTo(ImmutableMultimap.builder()
      .put(ACTIVE.name, new MapMarkerDto(logicalMeter.id, 1.222, 2.111, ACTIVE))
      .build()
      .asMap());
  }

  @Test
  public void mapSeveralGatewaysByStatusType() {
    Gateway gateway1 = Gateway.builder()
      .meter(logicalMeterWith(kungsbacka()))
      .statusLog(statusLogEntry(ACTIVE))
      .build();

    Gateway gateway2 = Gateway.builder()
      .meter(logicalMeterWith(kungsbacka().latitude(11.1234).longitude(2.123)))
      .statusLog(statusLogEntry(ACTIVE))
      .build();

    MapMarkersDto mapMarkersDto = MapMarkersDtoMapper.fromGateways(asList(gateway1, gateway2));

    assertThat(mapMarkersDto.markers).isEqualTo(ImmutableMultimap.builder()
      .put(ACTIVE.name, new MapMarkerDto(gateway1.id, 1.222, 2.111, ACTIVE))
      .put(ACTIVE.name, new MapMarkerDto(gateway2.id, 11.1234, 2.123, ACTIVE))
      .build()
      .asMap());
  }

  @Test
  public void mapSeveralLogicalMetersByStatusType() {
    LogicalMeter logicalMeter1 = logicalMeterWith(kungsbacka())
      .withPhysicalMeter(PhysicalMeter.builder()
        .status(statusLogEntry(ACTIVE))
        .build());

    LogicalMeter logicalMeter2 = logicalMeterWith(kungsbacka().latitude(11.1234).longitude(2.123))
      .withPhysicalMeter(PhysicalMeter.builder()
        .status(statusLogEntry(ACTIVE))
        .build());

    MapMarkersDto mapMarkersDto = MapMarkersDtoMapper.fromLogicalMeters(asList(
      logicalMeter1,
      logicalMeter2
    ));

    assertThat(mapMarkersDto.markers).isEqualTo(ImmutableMultimap.builder()
      .put(ACTIVE.name, new MapMarkerDto(logicalMeter1.id, 1.222, 2.111, ACTIVE))
      .put(ACTIVE.name, new MapMarkerDto(logicalMeter2.id, 11.1234, 2.123, ACTIVE))
      .build()
      .asMap());
  }

  @Test
  public void groupsGatewayMarkersByStatusType() {
    Gateway gateway1 = Gateway.builder()
      .meter(logicalMeterWith(kungsbacka()))
      .statusLog(statusLogEntry(ACTIVE))
      .build();

    Gateway gateway2 = Gateway.builder()
      .meter(logicalMeterWith(kungsbacka().latitude(11.1234).longitude(2.123)))
      .statusLog(statusLogEntry(ACTIVE))
      .build();

    Gateway gateway3 = Gateway.builder()
      .meter(
        new LogicalMeter(
          randomUUID(),
          randomUUID().toString(),
          randomUUID(),
          MeterDefinition.GAS_METER,
          kungsbacka().latitude(11.1234).longitude(12.123).build()
        ))
      .statusLog(statusLogEntry(WARNING))
      .build();

    MapMarkersDto mapMarkersDto = MapMarkersDtoMapper.fromGateways(asList(
      gateway1,
      gateway2,
      gateway3
    ));

    assertThat(mapMarkersDto.markers).isEqualTo(ImmutableMultimap.builder()
      .put(ACTIVE.name, new MapMarkerDto(gateway1.id, 1.222, 2.111, ACTIVE))
      .put(ACTIVE.name, new MapMarkerDto(gateway2.id, 11.1234, 2.123, ACTIVE))
      .put(WARNING.name, new MapMarkerDto(gateway3.id, 11.1234, 12.123, WARNING))
      .build()
      .asMap());
  }

  @Test
  public void groupsLogicalMeterMarkersByStatusType() {
    LogicalMeter logicalMeter1 = logicalMeterWith(kungsbacka())
      .withPhysicalMeter(PhysicalMeter.builder()
        .status(statusLogEntry(ACTIVE))
        .build());

    LogicalMeter logicalMeter2 = logicalMeterWith(kungsbacka().latitude(11.1234).longitude(2.123))
      .withPhysicalMeter(PhysicalMeter.builder()
        .status(statusLogEntry(ACTIVE))
        .build());

    LogicalMeter logicalMeter3 = logicalMeterWith(kungsbacka().latitude(11.1234).longitude(12.123))
      .withPhysicalMeter(PhysicalMeter.builder()
        .status(statusLogEntry(WARNING))
        .build());

    MapMarkersDto mapMarkersDto = MapMarkersDtoMapper.fromLogicalMeters(asList(
      logicalMeter1,
      logicalMeter2,
      logicalMeter3
    ));

    assertThat(mapMarkersDto.markers).isEqualTo(ImmutableMultimap.builder()
      .put(ACTIVE.name, new MapMarkerDto(logicalMeter1.id, 1.222, 2.111, ACTIVE))
      .put(ACTIVE.name, new MapMarkerDto(logicalMeter2.id, 11.1234, 2.123, ACTIVE))
      .put(WARNING.name, new MapMarkerDto(logicalMeter3.id, 11.1234, 12.123, WARNING))
      .build()
      .asMap());
  }

  private static StatusLogEntry<UUID> statusLogEntry(StatusType statusType) {
    return new StatusLogEntry<>(null, statusType, ZonedDateTime.now());
  }

  private static LogicalMeter logicalMeterWith(LocationBuilder locationBuilder) {
    return new LogicalMeter(
      randomUUID(),
      randomUUID().toString(),
      randomUUID(),
      MeterDefinition.HOT_WATER_METER,
      locationBuilder.build()
    );
  }

  private static LocationBuilder kungsbacka() {
    return new LocationBuilder()
      .country("sverige")
      .city("kungsbacka")
      .address("kabelgatan")
      .latitude(1.222)
      .longitude(2.111)
      .confidence(1.0);
  }
}
