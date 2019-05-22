package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;
import java.util.Collection;

import org.jooq.Condition;

import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.periodContains;

/**
 * Include all logical meters with current active physical meter if existing.
 */
class LogicalMeterFilterVisitor extends LogicalMeterMeasurementFilterVisitor {

  LogicalMeterFilterVisitor(Collection<FilterAcceptor> decorators) {
    super(decorators);
    addCondition(PHYSICAL_METER.ID.isNull().or(physicalMeterPeriodCondition()));
  }

  @Override
  Condition physicalMeterPeriodCondition() {
    return periodContains(PHYSICAL_METER.ACTIVE_PERIOD, OffsetDateTime.now());
  }
}
