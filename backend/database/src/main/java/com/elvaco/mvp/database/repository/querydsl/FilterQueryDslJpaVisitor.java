package com.elvaco.mvp.database.repository.querydsl;

import java.time.ZonedDateTime;
import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.QMeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.user.QOrganisationEntity;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.entity.measurement.QMeasurementEntity.measurementEntity;
import static com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity.missingMeasurementEntity;
import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity.meterAlarmLogEntity;
import static com.elvaco.mvp.database.entity.meter.QMeterDefinitionEntity.meterDefinitionEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.elvaco.mvp.database.entity.user.QOrganisationEntity.organisationEntity;

abstract class FilterQueryDslJpaVisitor implements FilterVisitor {

  protected static final Predicate FALSE_PREDICATE = Expressions.asBoolean(true).isFalse();

  static final QGatewayEntity GATEWAY = gatewayEntity;
  static final QLocationEntity LOCATION = locationEntity;
  static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG = gatewayStatusLogEntity;
  static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;
  static final QOrganisationEntity ORGANISATION = organisationEntity;
  static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG = physicalMeterStatusLogEntity;
  static final QMeterAlarmLogEntity ALARM_LOG = meterAlarmLogEntity;
  static final QMeasurementEntity MEASUREMENT = measurementEntity;
  static final QMissingMeasurementEntity MISSING_MEASUREMENT = missingMeasurementEntity;
  static final QMeterDefinitionEntity METER_DEFINITION = meterDefinitionEntity;

  protected abstract Collection<Predicate> getPredicates();

  protected abstract void applyJoins(JPQLQuery<?> q);

  public final void visitAndApply(Filters filters, JPQLQuery<?>... query) {
    filters.accept(this);
    for (JPQLQuery<?> q : query) {
      applyJoins(q);
      q.where(ExpressionUtils.allOf(getPredicates()));
    }
  }

  protected BooleanExpression withinPeriod(
    SelectionPeriod period,
    DateTimePath<ZonedDateTime> start,
    DateTimePath<ZonedDateTime> stop
  ) {
    return start.before(period.stop)
      .and(stop.isNull().or(stop.after(period.start)));
  }
}
