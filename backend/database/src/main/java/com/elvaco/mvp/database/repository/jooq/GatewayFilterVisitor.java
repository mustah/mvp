package com.elvaco.mvp.database.repository.jooq;

import java.util.Collection;

import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEDIUM;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog.GATEWAY_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static org.jooq.impl.DSL.max;

class GatewayFilterVisitor extends CommonFilterVisitor {

  private final DSLContext dsl;

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
    String value = filter.oneValue().toLowerCase();

    addCondition(GATEWAY.SERIAL.lower().startsWith(value)
      .or(GATEWAY.PRODUCT_MODEL.lower().startsWith(value))
      .or(LOCATION.CITY.lower().startsWith(value))
      .or(LOCATION.STREET_ADDRESS.lower().startsWith(value)));
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
              .and(GATEWAY_STATUS_LOG.STOP.isNull()))))))

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
      .on(METER_DEFINITION.ID.equal(LOGICAL_METER.METER_DEFINITION_ID))

      .leftJoin(MEDIUM)
      .on(MEDIUM.ID.eq(METER_DEFINITION.MEDIUM_ID))

      .leftJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(GATEWAY.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
