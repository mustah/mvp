package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;

@Slf4j
@RequiredArgsConstructor
public class MeteringAlarmMessageConsumer implements AlarmMessageConsumer {

  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final OrganisationUseCases organisationUseCases;
  private final MeterAlarmLogs meterAlarmLogs;

  @Override
  public Optional<GetReferenceInfoDto> accept(MeteringAlarmMessageDto message) {
    Organisation organisation = organisationUseCases.findOrCreate(message.organisationId);

    physicalMeterUseCases.findBy(
      organisation.id,
      message.facility.id,
      message.meter.id
    ).ifPresent(physicalMeter -> message.alarm.forEach(alarmDto ->
      meterAlarmLogs.createOrUpdate(
        physicalMeter.primaryKey(),
        alarmDto.mask,
        alarmDto.timestamp.atZone(METERING_TIMEZONE)
      )));

    return Optional.empty();
  }
}
