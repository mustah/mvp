package com.elvaco.mvp.database.repository.queryfilters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class GatewayQueryFilters extends QueryFilters {

  private static final QGatewayEntity Q = QGatewayEntity.gatewayEntity;

  @Override
  public Optional<Predicate> buildPredicateFor(String filter, List<String> values) {
    return Optional.ofNullable(buildNullablePredicateFor(filter, values));
  }

  @Nullable
  private Predicate buildNullablePredicateFor(String filter, List<String> values) {
    switch (filter) {
      case "id":
        return Q.id.in(mapValues(UUID::fromString, values));
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
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.countriesAndCities(parameters))
      .or(locationExpressions.unknownCities(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  @Nullable
  private Predicate whereAddress(Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.address(parameters))
      .or(locationExpressions.unknownAddress(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  private static LocationExpressions newLocationExpressions() {
    return new LocationExpressions(Q.meters.any().location);
  }
}
