package com.elvaco.mvp.database.repository.jooq;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;
import com.elvaco.mvp.core.filter.CollectionPeriodFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEDIUM;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.periodContains;
import static org.jooq.impl.DSL.noCondition;

class LogicalMeterFilterVisitor extends CommonFilterVisitor {

  private Condition physicalMeterCondition = noCondition();

  LogicalMeterFilterVisitor(Collection<FilterAcceptor> decorators) {
    super(decorators);
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(PeriodFilter filter) {
    FilterPeriod period = filter.getPeriod();

    physicalMeterCondition =
      periodContains(PHYSICAL_METER.ACTIVE_PERIOD, period.stop.toOffsetDateTime());
  }

  @Override
  public void visit(CollectionPeriodFilter filter) {
    FilterPeriod period = filter.getPeriod();

    if (physicalMeterCondition.equals(noCondition())) {
      physicalMeterCondition =
        periodContains(PHYSICAL_METER.ACTIVE_PERIOD, period.stop.toOffsetDateTime());
    }
  }

  @Override
  public void visit(WildcardFilter filter) {
    String value = filter.oneValue().toLowerCase();

    addCondition(LOGICAL_METER.EXTERNAL_ID.lower().contains(value)
      .or(MEDIUM.NAME.lower().contains(value))
      .or(LOCATION.CITY.lower().contains(value))
      .or(LOCATION.STREET_ADDRESS.lower().contains(value))
      .or(PHYSICAL_METER.MANUFACTURER.lower().contains(value))
      .or(PHYSICAL_METER.ADDRESS.lower().contains(value)));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.join(PHYSICAL_METER)
      .on(
        PHYSICAL_METER.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
          .and(PHYSICAL_METER.LOGICAL_METER_ID.equal(LOGICAL_METER.ID))
          .and(physicalMeterCondition)
      )

      .leftJoin(GATEWAYS_METERS)
      .on(GATEWAYS_METERS.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(GATEWAYS_METERS.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)))
      .leftJoin(GATEWAY)
      .on(GATEWAY.ORGANISATION_ID.equal(GATEWAYS_METERS.ORGANISATION_ID)
        .and(GATEWAY.ID.equal(GATEWAYS_METERS.GATEWAY_ID)))

      .innerJoin(METER_DEFINITION)
      .on(METER_DEFINITION.ID.equal(LOGICAL_METER.METER_DEFINITION_ID))

      .innerJoin(MEDIUM)
      .on(MEDIUM.ID.equal(METER_DEFINITION.MEDIUM_ID))

      /*This inner join is fine - a meter always has a location entry, although it might be NULL.
       * While a left join would be more "semantically" correct, this allows us to perform quick
       * sorting on location columns */
      .innerJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
