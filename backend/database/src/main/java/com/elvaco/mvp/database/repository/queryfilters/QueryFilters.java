package com.elvaco.mvp.database.repository.queryfilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.exception.PredicateConstructionFailure;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.user.QOrganisationEntity;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.core.util.CollectionUtils.isNotEmpty;
import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity.missingMeasurementEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity.meterAlarmLogEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.elvaco.mvp.database.entity.user.QOrganisationEntity.organisationEntity;

/**
 * A mapper of property filters to QueryDsl predicates.
 */
public abstract class QueryFilters {

  protected static final QMissingMeasurementEntity MISSING_MEASUREMENT = missingMeasurementEntity;
  protected static final QGatewayEntity GATEWAY = gatewayEntity;
  protected static final QOrganisationEntity ORGANISATION = organisationEntity;
  protected static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  protected static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;
  protected static final QMeterAlarmLogEntity ALARM_LOG = meterAlarmLogEntity;
  protected static final QLocationEntity LOCATION = QLocationEntity.locationEntity;
  protected static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG =
    physicalMeterStatusLogEntity;

  private static final Predicate[] NO_PREDICATE = new Predicate[0];

  public abstract Optional<Predicate> buildPredicateFor(
    RequestParameter parameter,
    RequestParameters parameters,
    List<String> values
  );

  /**
   * Should be used in WHERE statements.
   *
   * @param parameters to build the predicate from.
   *
   * @return a predicate or {@code null}.
   */
  @Nullable
  public final Predicate toExpression(RequestParameters parameters) {
    if (parameters.isEmpty()) {
      return null;
    }

    List<Predicate> predicates = new ArrayList<>();
    for (Entry<RequestParameter, List<String>> propertyFilter : parameters.entrySet()) {
      List<String> values = propertyFilter.getValue();
      if (isNotEmpty(values)) {
        RequestParameter parameter = propertyFilter.getKey();
        try {
          buildPredicateFor(parameter, parameters, values).ifPresent(predicates::add);
        } catch (Exception exception) {
          throw new PredicateConstructionFailure(parameter.toString(), values, exception);
        }
      }
    }

    if (predicates.isEmpty()) {
      return null;
    }

    return applyAndPredicates(predicates);
  }

  /**
   * Should mainly be used in JOIN ON statements.
   *
   * @param parameters to build the predicate from.
   *
   * @return non {@code null} predicate.
   */
  public final Predicate[] toPredicate(RequestParameters parameters) {
    return Optional.ofNullable(toExpression(parameters))
      .map(predicate -> new Predicate[] {predicate})
      .orElse(NO_PREDICATE);
  }

  private Predicate applyAndPredicates(List<Predicate> predicates) {
    return ExpressionUtils.allOf(predicates);
  }
}
