package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.StreetAddressFilter;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.knownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.knownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

class LocationFilterVisitor extends EmptyFilterVisitor {

  @Override
  public void visit(CityFilter filter) {
    if (filter.isWildcard()) {
      addCondition(LOCATION.CITY.lower().contains(filter.oneValue().toLowerCase()));
    } else {
      addCondition(knownCities(toCityParameters(filter.values())));
    }
  }

  @Override
  public void visit(StreetAddressFilter filter) {
    if (filter.isWildcard()) {
      addCondition(LOCATION.STREET_ADDRESS.lower().contains(filter.oneValue().toLowerCase()));
    } else {
      addCondition(knownAddresses(toAddressParameters(filter.values())));
    }
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(FacilityFilter filter) {
    addCondition(LOGICAL_METER.EXTERNAL_ID.in(filter.values()));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(LOGICAL_METER)
      .on(LOGICAL_METER.ID.equal(LOCATION.LOGICAL_METER_ID)
        .and(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)));
  }
}
