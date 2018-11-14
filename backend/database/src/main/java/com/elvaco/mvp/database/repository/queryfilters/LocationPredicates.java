package com.elvaco.mvp.database.repository.queryfilters;

import java.util.Collection;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

@UtilityClass
public class LocationPredicates {

  public static final QLocationEntity LOCATION = QLocationEntity.locationEntity;

  @Nullable
  public static Predicate whereCityOrUnknown(Collection<String> cityIds) {
    return whereCityOrUnknown(toCityParameters(cityIds));
  }

  @Nullable
  private static Predicate whereCityOrUnknown(Parameters parameters) {
    return new BooleanBuilder()
      .and(LocationPredicates.countriesAndCities(parameters))
      .or(LocationPredicates.unknownCities(parameters))
      .getValue();
  }

  @Nullable
  public static Predicate whereCityOrNull(Collection<String> cityIds) {
    return countriesAndCities(toCityParameters(cityIds));
  }

  @Nullable
  public static Predicate whereAddressOrUnknown(Collection<String> addressIds) {
    return whereAddressOrUnknown(toAddressParameters(addressIds));
  }

  @Nullable
  private static Predicate whereAddressOrUnknown(Parameters parameters) {
    return new BooleanBuilder()
      .and(LocationPredicates.address(parameters))
      .or(LocationPredicates.unknownAddress(parameters))
      .getValue();
  }

  @Nullable
  public static Predicate whereAddressOrNull(Collection<String> addressIds) {
    return address(toAddressParameters(addressIds));
  }

  @Nullable
  private static Predicate unknownCities(Parameters parameters) {
    return parameters.hasUnknownCities ? LOCATION.city.isNull() : null;
  }

  @Nullable
  private static Predicate unknownAddress(Parameters parameters) {
    return parameters.hasUnknownAddresses ? LOCATION.streetAddress.isNull() : null;
  }

  @Nullable
  private static Predicate countriesAndCities(Parameters parameters) {
    if (parameters.hasCountriesAndCities()) {
      return LOCATION.country.in(parameters.countries)
        .and(LOCATION.city.in(parameters.cities));
    }
    return null;
  }

  @Nullable
  private static Predicate address(Parameters parameters) {
    if (parameters.hasAddresses()) {
      return LOCATION.country.in(parameters.countries)
        .and(LOCATION.city.in(parameters.cities))
        .and(LOCATION.streetAddress.in(parameters.addresses));
    }
    return null;
  }
}
