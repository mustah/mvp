package com.elvaco.mvp.database.repository.queryfilters;

import javax.annotation.Nullable;

import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import lombok.experimental.UtilityClass;
import org.jooq.Condition;

import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static org.jooq.impl.DSL.noCondition;

@UtilityClass
public class LocationConditions {

  public static Condition cityOrUnknownFrom(Parameters parameters) {
    if (parameters.hasCountriesAndCities()) {
      return LOCATION.COUNTRY.in(parameters.countries)
        .and(LOCATION.CITY.in(parameters.cities))
        .or(parameters.hasUnknownCities ? LOCATION.CITY.isNull() : noCondition());
    }
    return noCondition();
  }

  @Nullable
  public static Condition addressOrUnknownFrom(Parameters parameters) {
    if (parameters.hasAddresses()) {
      return LOCATION.COUNTRY.in(parameters.countries)
        .and(LOCATION.CITY.in(parameters.cities))
        .and(LOCATION.STREET_ADDRESS.in(parameters.addresses))
        .or(parameters.hasUnknownAddresses ? LOCATION.STREET_ADDRESS.isNull() : noCondition());
    }
    return noCondition();
  }
}
