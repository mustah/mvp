package com.elvaco.mvp.database.repository.queryfilters;

import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;

import lombok.experimental.UtilityClass;
import org.jooq.Condition;

import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static org.jooq.impl.DSL.noCondition;

@UtilityClass
public class LocationConditions {

  public static Condition knownCities(Parameters parameters) {
    if (parameters.hasCountriesAndCities()) {
      return LOCATION.COUNTRY.lower().in(parameters.countries)
        .and(LOCATION.CITY.lower().in(parameters.cities));
    }
    return noCondition();
  }

  public static Condition withUnknownCities(Parameters parameters) {
    return knownCities(parameters)
      .or(parameters.hasUnknownCities ? LOCATION.CITY.isNull() : noCondition());
  }

  public static Condition knownAddresses(Parameters parameters) {
    if (parameters.hasAddresses()) {
      return LOCATION.COUNTRY.lower().in(parameters.countries)
        .and(LOCATION.CITY.lower().in(parameters.cities))
        .and(LOCATION.STREET_ADDRESS.lower().in(parameters.addresses));
    }
    return noCondition();
  }

  public static Condition withUnknownAddresses(Parameters parameters) {
    return knownAddresses(parameters)
      .or(parameters.hasUnknownAddresses ? LOCATION.STREET_ADDRESS.isNull() : noCondition());
  }
}
