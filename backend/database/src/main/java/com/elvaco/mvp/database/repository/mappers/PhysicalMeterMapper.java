package com.elvaco.mvp.database.repository.mappers;

import java.util.Collection;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class PhysicalMeterMapper implements DomainEntityMapper<PhysicalMeter, PhysicalMeterEntity> {

  private final OrganisationMapper organisationMapper;
  private final MeterStatusLogMapper meterStatusLogMapper;

  public PhysicalMeterMapper(
    OrganisationMapper organisationMapper,
    MeterStatusLogMapper meterStatusLogMapper
  ) {
    this.organisationMapper = organisationMapper;
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  @Override
  public PhysicalMeter toDomainModel(PhysicalMeterEntity entity) {
    return toDomainModel(entity, null);
  }

  public PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity, @Nullable Long measurementCount
  ) {
    return toDomainModel(entity, measurementCount, entity.statusLogs);
  }

  public PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity,
    @Nullable Long measurementCount,
    Collection<PhysicalMeterStatusLogEntity> statuses
  ) {
    return new PhysicalMeter(
      entity.id,
      organisationMapper.toDomainModel(entity.organisation),
      entity.address,
      entity.externalId,
      entity.medium,
      entity.manufacturer,
      entity.logicalMeterId,
      entity.readIntervalMinutes,
      measurementCount,
      statuses.stream().map(meterStatusLogMapper::toDomainModel).collect(toList())
    );
  }

  @Override
  public PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(
      domainModel.id,
      organisationMapper.toEntity(domainModel.organisation),
      domainModel.address,
      domainModel.externalId,
      domainModel.medium,
      domainModel.manufacturer,
      domainModel.logicalMeterId,
      domainModel.readIntervalMinutes,
      domainModel.statuses.stream().map(meterStatusLogMapper::toEntity).collect(toSet())
    );
  }
}
