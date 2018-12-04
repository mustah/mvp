package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.ManufacturerFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;
import com.elvaco.mvp.database.repository.queryfilters.FilterUtils;

import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog.METER_ALARM_LOG;

interface PhysicalMeterFilterVisitor extends FilterVisitor, ConditionAdding {

  default void visit(AlarmFilter alarmFilter) {
    addCondition(alarmFilter.values().stream()
      .anyMatch(FilterUtils::isYes)
      ? METER_ALARM_LOG.MASK.isNotNull()
      : METER_ALARM_LOG.MASK.isNull());
  }

  default void visit(MeterStatusFilter meterStatusFilter) {
    addCondition(PHYSICAL_METER_STATUS_LOG.STATUS.in(meterStatusFilter.values()));
  }

  default void visit(SecondaryAddressFilter secondaryAddressFilter) {
    addCondition(PHYSICAL_METER.ADDRESS.in(secondaryAddressFilter.values()));
  }

  default void visit(ManufacturerFilter manufacturerFilter) {
    addCondition(PHYSICAL_METER.MANUFACTURER.in(manufacturerFilter.values()));
  }
}
