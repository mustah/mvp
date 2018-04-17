package com.elvaco.mvp.database.repository.queryfilters;

import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.types.Predicate;

class LocationExpressions {

  private final QLocationEntity location;

  LocationExpressions(QLocationEntity location) {
    this.location = location;
  }

  @Nullable
  Predicate unknownCities(Parameters parameters) {
    return parameters.hasUnknownCities ? location.city.isNull() : null;
  }

  @Nullable
  Predicate hasLowConfidence(Parameters parameters) {
    return parameters.hasUnknownCities ? location.confidence.lt(0.75) : null;
  }

  @Nullable
  Predicate unknownAddress(Parameters parameters) {
    return parameters.hasUnknownAddresses ? location.streetAddress.isNull() : null;
  }

  @Nullable
  Predicate countriesAndCities(Parameters parameters) {
    if (parameters.hasCities()) {
      return location.country.toLowerCase().in(parameters.countries)
        .and(location.city.toLowerCase().in(parameters.cities));
    }
    return null;
  }

  @Nullable
  Predicate address(Parameters parameters) {
    if (parameters.hasAddresses()) {
      return location.country.toLowerCase().in(parameters.countries)
        .and(location.city.toLowerCase().in(parameters.cities))
        .and(location.streetAddress.toLowerCase().in(parameters.addresses));
    }
    return null;
  }
}
