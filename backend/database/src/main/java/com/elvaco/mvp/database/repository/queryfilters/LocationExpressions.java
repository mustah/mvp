package com.elvaco.mvp.database.repository.queryfilters;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;

class LocationExpressions {

  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;

  private final QLocationEntity location;

  private LocationExpressions(QLocationEntity location) {
    this.location = location;
  }

  @Nullable
  static Predicate whereCity(Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.countriesAndCities(parameters))
      .or(locationExpressions.unknownCities(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  @Nullable
  static Predicate whereAddress(Parameters parameters) {
    LocationExpressions locationExpressions = newLocationExpressions();
    return new BooleanBuilder()
      .and(locationExpressions.address(parameters))
      .or(locationExpressions.unknownAddress(parameters))
      .or(locationExpressions.hasLowConfidence(parameters))
      .getValue();
  }

  @Nullable
  private Predicate unknownCities(Parameters parameters) {
    return parameters.hasUnknownCities ? location.city.isNull() : null;
  }

  @Nullable
  private Predicate unknownAddress(Parameters parameters) {
    return parameters.hasUnknownAddresses ? location.streetAddress.isNull() : null;
  }

  @Nullable
  private Predicate hasLowConfidence(Parameters parameters) {
    return parameters.hasUnknownCities
      ? location.confidence.lt(GeoCoordinate.HIGH_CONFIDENCE).or(location.confidence.isNull())
      : null;
  }

  @Nullable
  private Predicate countriesAndCities(Parameters parameters) {
    if (parameters.hasCountriesAndCities()) {
      return location.country.toLowerCase().in(parameters.countries)
        .and(location.city.toLowerCase().in(parameters.cities));
    }
    return null;
  }

  @Nullable
  private Predicate address(Parameters parameters) {
    if (parameters.hasAddresses()) {
      return location.country.toLowerCase().in(parameters.countries)
        .and(location.city.toLowerCase().in(parameters.cities))
        .and(location.streetAddress.toLowerCase().in(parameters.addresses));
    }
    return null;
  }

  private static LocationExpressions newLocationExpressions() {
    return new LocationExpressions(LOGICAL_METER.location);
  }
}
