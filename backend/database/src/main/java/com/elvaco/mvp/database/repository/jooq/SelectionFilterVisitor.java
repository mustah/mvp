package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;
import com.elvaco.mvp.core.filter.StreetAddressFilter;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

class SelectionFilterVisitor extends EmptyFilterVisitor {

  @Override
  public void visit(CityFilter filter) {
    addCondition(withUnknownCities(toCityParameters(filter.values())));
  }

  @Override
  public void visit(StreetAddressFilter filter) {
    addCondition(withUnknownAddresses(toAddressParameters(filter.values())));
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(FacilityFilter filter) {
    if (filter.isWildcard()) {
      addCondition(LOGICAL_METER.EXTERNAL_ID.lower().contains(filter.oneValue().toLowerCase()));
    } else {
      addCondition(LOGICAL_METER.EXTERNAL_ID.in(filter.values()));
    }
  }

  @Override
  public void visit(SecondaryAddressFilter filter) {
    if (filter.isWildcard()) {
      addCondition(PHYSICAL_METER.ADDRESS.lower().contains(filter.oneValue().toLowerCase()));
    } else {
      addCondition(PHYSICAL_METER.ADDRESS.in(filter.values()));
    }
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(PHYSICAL_METER)
      .on(LOGICAL_METER.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(LOGICAL_METER.ID.equal(PHYSICAL_METER.LOGICAL_METER_ID)))

      .leftJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
