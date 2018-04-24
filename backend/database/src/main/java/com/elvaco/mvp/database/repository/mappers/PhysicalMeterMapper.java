package com.elvaco.mvp.database.repository.mappers;

import java.util.Collection;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class PhysicalMeterMapper {

  public static PhysicalMeter toDomainModel(PhysicalMeterEntity entity) {
    return toDomainModel(entity, null);
  }

  public static PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity,
    @Nullable Long measurementCount
  ) {
    return toDomainModel(entity, measurementCount, entity.statusLogs);
  }

  public static PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity,
    @Nullable Long measurementCount,
    Collection<PhysicalMeterStatusLogEntity> statuses
  ) {
    return new PhysicalMeter(
      entity.id,
      OrganisationMapper.toDomainModel(entity.organisation),
      entity.address,
      entity.externalId,
      entity.medium,
      entity.manufacturer,
      entity.logicalMeterId,
      entity.readIntervalMinutes,
      measurementCount,
      statuses.stream().map(MeterStatusLogMapper::toDomainModel).collect(toList())
    );
  }

  public static PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(
      domainModel.id,
      OrganisationMapper.toEntity(domainModel.organisation),
      domainModel.address,
      domainModel.externalId,
      domainModel.medium,
      domainModel.manufacturer,
      domainModel.logicalMeterId,
      domainModel.readIntervalMinutes,
      domainModel.statuses.stream().map(MeterStatusLogMapper::toEntity).collect(toSet())
    );
  }
}
