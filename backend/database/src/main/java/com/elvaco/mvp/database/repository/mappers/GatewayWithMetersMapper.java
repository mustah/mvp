package com.elvaco.mvp.database.repository.mappers;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import static java.util.stream.Collectors.toList;

public class GatewayWithMetersMapper {

  private final LogicalMeterMapper logicalMeterMapper;

  public GatewayWithMetersMapper(LogicalMeterMapper logicalMeterMapper) {
    this.logicalMeterMapper = logicalMeterMapper;
  }

  public Gateway withLogicalMeters(GatewayEntity entity) {
    return new Gateway(
      entity.id,
      entity.serial,
      entity.productModel,
      entity.phoneNumber,
      entity.port,
      entity.ip,
      toLogicalMeters(entity.meters)
    );
  }

  private List<LogicalMeter> toLogicalMeters(List<LogicalMeterEntity> meters) {
    return meters
      .stream()
      .map(logicalMeterMapper::toDomainModel)
      .collect(toList());
  }
}
