package com.elvaco.mvp.database;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.Units;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.mappers.GatewayEntityMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

public class LogicalMeterJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private LogicalMeterEntityMapper logicalMeterEntityMapper;

  @Test
  public void locationIsPersisted() {
    LogicalMeter logicalMeter = given(
      logicalMeter().location(
        new LocationBuilder().latitude(1.0).longitude(2.0).confidence(1.0).build()
      )
    );

    LogicalMeterEntity foundEntity = logicalMeterJpaRepository.findById(logicalMeter.id).get();

    assertThat(foundEntity.location.confidence).isEqualTo(1.0);
    assertThat(foundEntity.location.latitude).isEqualTo(1.0);
    assertThat(foundEntity.location.longitude).isEqualTo(2.0);
  }

  @Test
  public void physicalMetersAreFetched() {
    LogicalMeter logicalMeter = given(logicalMeter(), physicalMeter());

    assertThat(logicalMeterJpaRepository.findById(logicalMeter.id).get().physicalMeters)
      .isNotEmpty();
  }

  @Test
  @Transactional
  public void gatewayAndPhysicalMeterRemainsAtDelete() {
    var logicalMeter = given(logicalMeter().gateway(gateway().build()), physicalMeter());
    assertThat(logicalMeter.gateways).hasSize(1);
    assertThat(logicalMeter.physicalMeters).hasSize(1);

    var gatewayId = logicalMeter.gateways.get(0).id;
    var physicalMeterId = logicalMeter.physicalMeters.get(0).id;

    commitTransaction();

    var savedLogicalMeter = logicalMeterJpaRepository.findById(logicalMeter.id).get();
    assertThat(savedLogicalMeter.gateways).extracting(qw -> qw.pk.id).containsExactly(gatewayId);
    assertThat(savedLogicalMeter.physicalMeters).extracting(m -> m.id).containsExactly(
      physicalMeterId);

    logicalMeterJpaRepository.delete(logicalMeter.id, logicalMeter.organisationId);

    commitTransaction();

    assertThat(gatewayJpaRepository.findById(gatewayId)).isPresent();

    // TODO physical meter should remain, is deleted with database constraint
    assertThat(physicalMeterJpaRepository.findById(physicalMeterId)).isNotPresent();
  }

  @Test
  public void physicalMeterIsNotUpdatedWithLogicalMeter() {
    var logicalMeter = logicalMeterJpaRepository.save(
      logicalMeterEntityMapper.toEntity(logicalMeter().build()));

    var physicalMeter = physicalMeter().logicalMeterId(logicalMeter.getId().id).build();
    physicalMeterJpaRepository.save(PhysicalMeterEntityMapper.toEntity(physicalMeter));

    var newPhysicalMeter = physicalMeter.toBuilder().address("newAddress").build();

    logicalMeterJpaRepository.save(logicalMeterEntityMapper.toEntity(
      logicalMeterEntityMapper.toDomainModel(logicalMeter)
        .toBuilder().physicalMeter(newPhysicalMeter).build()));

    assertThat(logicalMeterJpaRepository.findById(logicalMeter.pk.id).get().physicalMeters)
      .extracting(p -> p.address)
      .containsOnly(physicalMeter.address);

    assertThat(physicalMeterJpaRepository.findById(physicalMeter.id).get().address)
      .isEqualTo(physicalMeter.address);
  }

  @Test
  public void gatewayIsUpdatedWithLogicalMeter() {
    var logicalMeter = logicalMeterJpaRepository.save(
      logicalMeterEntityMapper.toEntity(logicalMeter().build()));

    var gateway = gateway().meter(logicalMeterEntityMapper.toDomainModel(logicalMeter)).build();
    gatewayJpaRepository.save(GatewayEntityMapper.toEntity(gateway));

    var newGateway = gateway.toBuilder().serial("newSerial").build();
    logicalMeterJpaRepository.save(logicalMeterEntityMapper.toEntity(
      logicalMeterEntityMapper.toDomainModel(logicalMeter)
        .toBuilder().gateway(newGateway).build()));

    assertThat(logicalMeterJpaRepository.findById(logicalMeter.pk.id).get().gateways)
      .extracting(p -> p.serial)
      .containsOnly("newSerial");

    assertThat(gatewayJpaRepository.findById(gateway.id).get().serial)
      .isEqualTo("newSerial");
  }

  @Test
  public void getPreferredQuantityParametersForLogicalMeters() {
    var displayQuantity1 = new DisplayQuantity(
      Quantity.POWER,
      DisplayMode.READOUT,
      2,
      Units.WATT
    );
    var displayQuantity2 = new DisplayQuantity(
      Quantity.POWER,
      DisplayMode.READOUT,
      2,
      Units.KILOWATT
    );

    var meterDefintion1 = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
      .quantities(Set.of(displayQuantity1)));
    var meterDefintion2 = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
      .quantities(Set.of(displayQuantity2)));
    var meter1a = given(logicalMeter().meterDefinition(meterDefintion1));
    var meter2 = given(logicalMeter().meterDefinition(meterDefintion2));
    var meter1b = given(logicalMeter().meterDefinition(meterDefintion1));

    RequestParameters parameters = idParametersOf(meter1a, meter2, meter1b);

    assertThat(logicalMeterJpaRepository.getPreferredQuantityParameters(parameters))
      .hasSize(1)
      .extracting(qp -> qp.name, qp -> qp.unit, qp -> qp.isConsumption())
      .containsExactly(tuple(Quantity.POWER.name, Units.WATT, false));
  }

  @Test
  public void getPreferredQuantityParametersForLogicalMeters_withoutDisplayQuantities() {
    var meterDefintion = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.UNKNOWN_MEDIUM)));
    var meter = given(logicalMeter().meterDefinition(meterDefintion));

    RequestParameters parameters = idParametersOf(meter);

    assertThat(logicalMeterJpaRepository.getPreferredQuantityParameters(parameters)).hasSize(0);
  }

  private RequestParameters idParametersOf(LogicalMeter... meters) {
    return RequestParametersAdapter.of(Map.of(
      LOGICAL_METER_ID.toString(),
      Arrays.stream(meters).map(meter -> meter.id.toString()).collect(toList())
    ));
  }

  private static void commitTransaction() {
    TestTransaction.flagForCommit();
    TestTransaction.end();
    TestTransaction.start();
  }
}
