package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.MediumFilter;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;

interface LogicalMeterFilterVisitor extends FilterVisitor, ConditionAdding {

  @Override
  default void visit(MediumFilter filter) {
    addCondition(METER_DEFINITION.MEDIUM.in(filter.values()));
  }

  @Override
  default void visit(FacilityFilter filter) {
    addCondition(LOGICAL_METER.EXTERNAL_ID.in(filter.values()));
  }

  @Override
  default void visit(LogicalMeterIdFilter filter) {
    addCondition(LOGICAL_METER.ID.in(filter.values()));
  }
}
