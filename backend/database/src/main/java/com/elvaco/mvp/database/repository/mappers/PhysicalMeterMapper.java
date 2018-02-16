package com.elvaco.mvp.database.repository.mappers;

import java.util.ArrayList;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;

import static java.util.stream.Collectors.toList;

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
    List<MeterStatusLog> statusLogs = new ArrayList<>();
    if (entity.statusLogs != null) {
      statusLogs = entity.statusLogs.stream()
        .map(meterStatusLogMapper::toDomainModel).collect(toList());
    }

    return new PhysicalMeter(
      entity.id,
      organisationMapper.toDomainModel(entity.organisation),
      entity.identity,
      entity.medium,
      entity.manufacturer,
      entity.logicalMeterId,
      statusLogs
    );
  }

  @Override
  public PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(
      domainModel.id,
      organisationMapper.toEntity(domainModel.organisation),
      domainModel.identity,
      domainModel.medium,
      domainModel.manufacturer,
      domainModel.logicalMeterId
    );
  }
}
