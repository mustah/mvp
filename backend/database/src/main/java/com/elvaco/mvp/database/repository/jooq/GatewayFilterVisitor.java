package com.elvaco.mvp.database.repository.jooq;

import java.sql.Date;
import java.time.LocalDate;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.MeasurementThresholdFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog.GATEWAY_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.select;

@RequiredArgsConstructor
class GatewayFilterVisitor extends CommonFilterVisitor {

  private final DSLContext dsl;
  private final MeasurementThresholdParser measurementThresholdParser;

  private Condition gatewayStatusLogCondition = falseCondition();
  private Condition alarmLogCondition = falseCondition();
  private Condition meterStatusLogCondition = falseCondition();
  private Condition measurementStatsCondition = falseCondition();
  private Condition measurementStatsFilter = falseCondition();

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
          .or(GATEWAY_STATUS_LOG.STOP.greaterOrEqual(period.start.toOffsetDateTime())));

    meterStatusLogCondition =
      PHYSICAL_METER_STATUS_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(PHYSICAL_METER_STATUS_LOG.STOP.isNull()
          .or(PHYSICAL_METER_STATUS_LOG.STOP.greaterOrEqual(period.start.toOffsetDateTime())));

    alarmLogCondition =
      METER_ALARM_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(METER_ALARM_LOG.STOP.isNull()
          .or(METER_ALARM_LOG.STOP.greaterOrEqual(period.start.toOffsetDateTime())));

    LocalDate startDate = period.start.toLocalDate();
    LocalDate stopDate = period.stop.toLocalDate();
    if (stopDate.isEqual(startDate)) {
      measurementStatsCondition = MEASUREMENT_STAT_DATA.STAT_DATE.equal(Date.valueOf(startDate))
        .and(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
    } else {
      measurementStatsCondition =
        MEASUREMENT_STAT_DATA.STAT_DATE.greaterOrEqual(Date.valueOf(startDate))
          .and(MEASUREMENT_STAT_DATA.STAT_DATE.lessThan(Date.valueOf(stopDate)))
          .and(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
    }
  }

  @Override
  public void visit(MeasurementThresholdFilter filter) {
    MeasurementThreshold threshold = measurementThresholdParser.parse(filter.oneValue());

    measurementStatsFilter = MEASUREMENT_STAT_DATA.QUANTITY.equal(threshold.quantity.getId())
      .and(valueConditionFor(threshold));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    if (!measurementStatsFilter.equals(falseCondition())
      && !measurementStatsCondition.equals(falseCondition())) {
      addCondition(exists(select()
        .from(MEASUREMENT_STAT_DATA)
        .where(measurementStatsFilter.and(measurementStatsCondition))));
    }

    return query.leftJoin(GATEWAY_STATUS_LOG)
      .on(GATEWAY_STATUS_LOG.GATEWAY_ID.equal(GATEWAY.ID)
        .and(GATEWAY_STATUS_LOG.ORGANISATION_ID.equal(GATEWAY.ORGANISATION_ID))
        .and(GATEWAY_STATUS_LOG.ID.equal(dsl
          .select(DSL.max(GATEWAY_STATUS_LOG.ID))
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

      .leftJoin(PHYSICAL_METER_STATUS_LOG)
      .on(PHYSICAL_METER_STATUS_LOG.ORGANISATION_ID
        .equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
        .and(PHYSICAL_METER_STATUS_LOG.ID.equal(dsl
          .select(max(PHYSICAL_METER_STATUS_LOG.ID))
          .from(PHYSICAL_METER_STATUS_LOG)
          .where(PHYSICAL_METER_STATUS_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
            .and(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
              .and(meterStatusLogCondition))))))

      .leftJoin(METER_ALARM_LOG)
      .on(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
        .and(METER_ALARM_LOG.ID.equal(dsl
          .select(max(METER_ALARM_LOG.ID))
          .from(METER_ALARM_LOG)
          .where(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
            .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
              .and(alarmLogCondition))))))

      .leftJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(GATEWAY.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
