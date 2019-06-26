package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.StreetAddressFilter;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

interface CommonLocationFilterVisitor extends ConditionAdding, FilterVisitor {

  @Override
  default void visit(CityFilter filter) {
    addCondition(withUnknownCities(toCityParameters(filter.values())));
  }

  @Override
  default void visit(StreetAddressFilter filter) {
    addCondition(withUnknownAddresses(toAddressParameters(filter.values())));
  }

  @Override
  default void visit(LocationConfidenceFilter filter) {
    addCondition(LOCATION.CONFIDENCE.greaterOrEqual(filter.oneValue()));
  }
}
