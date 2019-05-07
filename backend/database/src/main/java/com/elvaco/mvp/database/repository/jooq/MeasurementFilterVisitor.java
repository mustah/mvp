package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.ReportPeriodFilter;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;

class MeasurementFilterVisitor extends EmptyFilterVisitor {

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(ReportPeriodFilter filter) {
    FilterPeriod period = filter.getPeriod();

    addCondition(MEASUREMENT.CREATED
      .greaterOrEqual(period.start.toOffsetDateTime())
      .and(MEASUREMENT.CREATED.lessThan(period.stop.toOffsetDateTime())));
  }

  @Override
  public void visit(FacilityFilter filter) {
    addCondition(LOGICAL_METER.EXTERNAL_ID.in(filter.values()));
  }

  @Override
  public void visit(LogicalMeterIdFilter filter) {
    addCondition(LOGICAL_METER.ID.in(filter.values()));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.join(PHYSICAL_METER)
      .on(MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))

      .join(LOGICAL_METER)
      .on(LOGICAL_METER.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(LOGICAL_METER.ID.equal(PHYSICAL_METER.LOGICAL_METER_ID)));
  }
}
