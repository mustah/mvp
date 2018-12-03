package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.MediumFilter;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;

interface LogicalMeterFilterVisitor extends FilterVisitor, ConditionAdding {

  default void visit(MediumFilter mediumFilter) {
    addCondition(METER_DEFINITION.MEDIUM.in(mediumFilter.values()));
  }

  default void visit(FacilityFilter facilityFilter) {
    addCondition(LOGICAL_METER.EXTERNAL_ID.in(facilityFilter.values()));
  }

  default void visit(LogicalMeterIdFilter logicalMeterIdFilter) {
    addCondition(LOGICAL_METER.ID.in(logicalMeterIdFilter.values()));
  }
}
