package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.ManufacturerFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;

import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog.METER_ALARM_LOG;

interface PhysicalMeterFilterVisitor extends FilterVisitor, ConditionAdding {

  @Override
  default void visit(AlarmFilter filter) {
    addCondition(filter.values().stream()
      .anyMatch("yes"::equalsIgnoreCase)
      ? METER_ALARM_LOG.MASK.isNotNull()
      : METER_ALARM_LOG.MASK.isNull());
  }

  @Override
  default void visit(MeterStatusFilter filter) {
    addCondition(PHYSICAL_METER_STATUS_LOG.STATUS.in(filter.values()));
  }

  @Override
  default void visit(SecondaryAddressFilter filter) {
    addCondition(PHYSICAL_METER.ADDRESS.in(filter.values()));
  }

  @Override
  default void visit(ManufacturerFilter filter) {
    addCondition(PHYSICAL_METER.MANUFACTURER.in(filter.values()));
  }
}
