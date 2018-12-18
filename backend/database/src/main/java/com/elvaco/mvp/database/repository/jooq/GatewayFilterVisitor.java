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
import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog.GATEWAY_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.trueCondition;

class GatewayFilterVisitor extends CommonFilterVisitor {

  private final DSLContext dsl;

  private Condition gatewayStatusLogCondition = falseCondition();
  private Condition alarmLogCondition = falseCondition();
  private Condition meterStatusLogCondition = falseCondition();

  GatewayFilterVisitor(DSLContext dsl, Collection<FilterAcceptor> decorators) {
    super(decorators);
    this.dsl = dsl;
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(GATEWAY.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(WildcardFilter filter) {
    var value = filter.oneValue().toLowerCase();

    addCondition(GATEWAY.SERIAL.lower().startsWith(value)
      .or(GATEWAY.PRODUCT_MODEL.lower().startsWith(value))
      .or(LOCATION.CITY.lower().startsWith(value))
      .or(LOCATION.STREET_ADDRESS.lower().startsWith(value)));
  }

  @Override
  public void visit(PeriodFilter filter) {
    SelectionPeriod period = filter.getPeriod();

    gatewayStatusLogCondition =
      GATEWAY_STATUS_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(GATEWAY_STATUS_LOG.STOP.isNull()
          .or(GATEWAY_STATUS_LOG.STOP.greaterOrEqual(period.stop.toOffsetDateTime())));

    meterStatusLogCondition =
      PHYSICAL_METER_STATUS_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(PHYSICAL_METER_STATUS_LOG.STOP.isNull()
          .or(PHYSICAL_METER_STATUS_LOG.STOP.greaterOrEqual(period.stop.toOffsetDateTime())));

    alarmLogCondition =
      METER_ALARM_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(METER_ALARM_LOG.STOP.isNull()
          .or(METER_ALARM_LOG.STOP.greaterOrEqual(period.stop.toOffsetDateTime())));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(GATEWAY_STATUS_LOG)
      .on(GATEWAY_STATUS_LOG.GATEWAY_ID.equal(GATEWAY.ID)
        .and(GATEWAY_STATUS_LOG.ORGANISATION_ID.equal(GATEWAY.ORGANISATION_ID))
        .and(GATEWAY_STATUS_LOG.ID.equal(dsl
          .select(max(GATEWAY_STATUS_LOG.ID))
          .from(GATEWAY_STATUS_LOG)
          .where(GATEWAY_STATUS_LOG.GATEWAY_ID.equal(GATEWAY.ID)
            .and(GATEWAY_STATUS_LOG.ORGANISATION_ID.equal(GATEWAY.ORGANISATION_ID)
              .and(gatewayStatusLogCondition))))))

      .leftJoin(GATEWAYS_METERS)
      .on(GATEWAYS_METERS.GATEWAY_ID.equal(GATEWAY.ID)
        .and(GATEWAYS_METERS.ORGANISATION_ID.equal(GATEWAY.ORGANISATION_ID)))

      .leftJoin(LOGICAL_METER)
      .on(LOGICAL_METER.ID.equal(GATEWAYS_METERS.LOGICAL_METER_ID)
        .and(GATEWAY.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)))

      .leftJoin(PHYSICAL_METER)
      .on(PHYSICAL_METER.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(PHYSICAL_METER.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)))

      .leftJoin(METER_DEFINITION)
      .on(METER_DEFINITION.TYPE.equal(LOGICAL_METER.METER_DEFINITION_TYPE))

      .leftJoin(lateral(dsl
          .select(PHYSICAL_METER_STATUS_LOG.STATUS)
          .from(PHYSICAL_METER_STATUS_LOG)
          .where(PHYSICAL_METER_STATUS_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
            .and(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
            .and(meterStatusLogCondition))
          .orderBy(PHYSICAL_METER_STATUS_LOG.START)
          .limit(1)
        .asTable(PHYSICAL_METER_STATUS_LOG.getName()))
      ).on(trueCondition())

      .leftJoin(lateral(dsl
          .select(METER_ALARM_LOG.MASK)
          .from(METER_ALARM_LOG)
          .where(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
            .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
            .and(alarmLogCondition))
          .orderBy(METER_ALARM_LOG.START)
          .limit(1)
        .asTable(METER_ALARM_LOG.getName()))
      ).on(trueCondition())

      .leftJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(GATEWAY.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
