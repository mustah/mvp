package com.elvaco.mvp.database.repository.mappers;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class PhysicalMeterEntityMapper {

  public static PhysicalMeter toDomainModelWithoutStatusLogs(PhysicalMeterEntity entity) {
    return toDomainModel(entity, emptyList());
  }

  public static PhysicalMeter toDomainModel(PhysicalMeterEntity entity) {
    return toDomainModel(entity, entity.statusLogs);
  }

  public static PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity,
    Collection<PhysicalMeterStatusLogEntity> statuses
  ) {
    return new PhysicalMeter(
      entity.id,
      OrganisationEntityMapper.toDomainModel(entity.organisation),
      entity.address,
      entity.externalId,
      entity.medium,
      entity.manufacturer,
      entity.logicalMeterId,
      entity.readIntervalMinutes,
      statuses.stream().map(MeterStatusLogEntityMapper::toDomainModel).collect(toList())
    );
  }

  public static PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(
      domainModel.id,
      OrganisationEntityMapper.toEntity(domainModel.organisation),
      domainModel.address,
      domainModel.externalId,
      domainModel.medium,
      domainModel.manufacturer,
      domainModel.logicalMeterId,
      domainModel.readIntervalMinutes,
      domainModel.statuses.stream().map(MeterStatusLogEntityMapper::toEntity).collect(toSet())
    );
  }
}
