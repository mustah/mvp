package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.DISPLAY_QUANTITY;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.Organisation.ORGANISATION;

class DisplayQuantityFilterVisitor extends EmptyFilterVisitor {

  public void visit(OrganisationIdFilter filter) {
    addCondition(ORGANISATION.ID.in(filter.values()));
  }

  public void visit(LogicalMeterIdFilter filter) {
    addCondition(LOGICAL_METER.ID.in(filter.values()));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query
      .innerJoin(METER_DEFINITION)
      .on(METER_DEFINITION.ID.equal(LOGICAL_METER.METER_DEFINITION_ID))
      .join(DISPLAY_QUANTITY)
      .on(METER_DEFINITION.ID.eq(DISPLAY_QUANTITY.METER_DEFINITION_ID))
      .leftJoin(QUANTITY).on(QUANTITY.ID.eq(DISPLAY_QUANTITY.QUANTITY_ID));
  }
}
