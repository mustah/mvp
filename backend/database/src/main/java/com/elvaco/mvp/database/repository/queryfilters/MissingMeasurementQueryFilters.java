package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity.missingMeasurementEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.gatewayStatusQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.getZonedDateTimeFrom;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.meterStatusQueryFilter;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toStatusTypes;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.toUuids;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class MissingMeasurementQueryFilters extends QueryFilters {

  private static final QMissingMeasurementEntity MISSING_MEASUREMENT = missingMeasurementEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  private static final QGatewayEntity GATEWAY = gatewayEntity;

  private ZonedDateTime before;
  private ZonedDateTime after;

  @Override
  public Optional<Predicate> buildPredicateFor(String parameterName, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(parameterName, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "id":
        return MISSING_MEASUREMENT.id.physicalMeter.logicalMeterId.in(toUuids(values));
      case "physicalMeterId":
        return MISSING_MEASUREMENT.id.physicalMeter.id.in(toUuids(values));
      case "manufacturer":
        return MISSING_MEASUREMENT.id.physicalMeter.manufacturer.in(values);
      case "secondaryAddress":
        return MISSING_MEASUREMENT.id.physicalMeter.address.in(values);
      case "organisation":
        return MISSING_MEASUREMENT.id.physicalMeter.organisation.id.in(toUuids(values));
      case "before":
        before = getZonedDateTimeFrom(values);
        return MISSING_MEASUREMENT.id.expectedTime.lt(before);
      case "after":
        after = getZonedDateTimeFrom(values);
        return MISSING_MEASUREMENT.id.expectedTime.goe(after);
      case "city":
        return LocationExpressions.whereCity(toCityParameters(values));
      case "address":
        return LocationExpressions.whereAddress(toAddressParameters(values));
      case "facility":
        return LOGICAL_METER.externalId.in(values);
      case "medium":
        return LOGICAL_METER.meterDefinition.medium.in(values);
      case "status":
      case "meterStatus":
        return meterStatusQueryFilter(after, before, toStatusTypes(values));
      case "gatewayStatus":
        return gatewayStatusQueryFilter(after, before, toStatusTypes(values));
      case "gatewaySerial":
        return GATEWAY.serial.in(values);
      default:
        return null;
    }
  }
}
