package com.elvaco.mvp.database.repository.jooq;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.MISSING_MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.MISSING_MEASUREMENT_COUNT;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.trueCondition;

class LogicalMeterFilterVisitor extends CommonFilterVisitor {

  private final DSLContext dsl;

  private Condition missingMeasurementCondition = falseCondition();

  public LogicalMeterFilterVisitor(DSLContext dsl, Collection<FilterAcceptor> decorators) {
    super(decorators);
    this.dsl = dsl;
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(WildcardFilter filter) {
    String value = filter.oneValue().toLowerCase();

    addCondition(LOGICAL_METER.EXTERNAL_ID.lower().startsWith(value)
      .or(METER_DEFINITION.MEDIUM.lower().startsWith(value))
      .or(LOCATION.CITY.lower().startsWith(value))
      .or(LOCATION.STREET_ADDRESS.lower().startsWith(value))
      .or(PHYSICAL_METER.MANUFACTURER.lower().startsWith(value))
      .or(PHYSICAL_METER.ADDRESS.lower().startsWith(value)));
  }

  @Override
  public void visit(PeriodFilter filter) {
    SelectionPeriod period = filter.getPeriod();

    missingMeasurementCondition =
      MISSING_MEASUREMENT.EXPECTED_TIME.greaterOrEqual(period.start.toOffsetDateTime())
        .and(MISSING_MEASUREMENT.EXPECTED_TIME.lessThan(period.stop.toOffsetDateTime()));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(PHYSICAL_METER)
      .on(PHYSICAL_METER.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(PHYSICAL_METER.LOGICAL_METER_ID.equal(LOGICAL_METER.ID))
      )

      .leftJoin(GATEWAYS_METERS)
      .on(GATEWAYS_METERS.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(GATEWAYS_METERS.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)))
      .leftJoin(GATEWAY)
      .on(GATEWAY.ORGANISATION_ID.equal(GATEWAYS_METERS.ORGANISATION_ID)
        .and(GATEWAY.ID.equal(GATEWAYS_METERS.GATEWAY_ID)))

      .leftJoin(METER_DEFINITION)
      .on(METER_DEFINITION.TYPE.equal(LOGICAL_METER.METER_DEFINITION_TYPE))

      .leftJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)))

      .leftJoin(lateral(
        dsl.select(count().as(MISSING_MEASUREMENT_COUNT))
          .from(MISSING_MEASUREMENT)
          .where(MISSING_MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
            .and(missingMeasurementCondition))
          .groupBy(MISSING_MEASUREMENT.PHYSICAL_METER_ID))
        .asTable("mm"))
      .on(trueCondition());
  }
}
