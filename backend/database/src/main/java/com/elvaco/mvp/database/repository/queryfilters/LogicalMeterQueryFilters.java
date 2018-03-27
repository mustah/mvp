package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class LogicalMeterQueryFilters extends QueryFilters {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "id":
        return Q.id.in(mapValues(UUID::fromString, values));
      case "medium":
        return Q.meterDefinition.medium.in(values);
      case "manufacturer":
        return Q.physicalMeters.any().manufacturer.in(values);
      case "organisation":
        return Q.organisationId.in(mapValues(UUID::fromString, values));
      case "city":
        return whereCity(toCityParameters(values));
      case "address":
        return whereAddress(toAddressParameters(values));
      default:
        return null;
    }
  }

  @Nullable
  private Predicate whereCity(Parameters parameters) {
    if (parameters.hasCities()) {
      return Q.location.country.toLowerCase().in(parameters.countries)
        .and(Q.location.city.toLowerCase().in(parameters.cities));
    }
    return null;
  }

  @Nullable
  private Predicate whereAddress(Parameters parameters) {
    if (parameters.hasAddresses()) {
      return Q.location.country.toLowerCase().in(parameters.countries)
        .and(Q.location.city.toLowerCase().in(parameters.cities))
        .and(Q.location.streetAddress.toLowerCase().in(parameters.addresses));
    }
    return null;
  }
}
