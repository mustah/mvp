package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public interface LocationFilterVisitor extends ConditionAdding, FilterVisitor {

  default void visit(CityFilter cityFilter) {
    addCondition(withUnknownCities(toCityParameters(cityFilter.values())));
  }

  default void visit(AddressFilter addressFilter) {
    addCondition(withUnknownAddresses(toAddressParameters(addressFilter.values())));
  }

  default void visit(LocationConfidenceFilter locationConfidenceFilter) {
    addCondition(LOCATION.CONFIDENCE.greaterOrEqual(locationConfidenceFilter.oneValue()));
  }
}
