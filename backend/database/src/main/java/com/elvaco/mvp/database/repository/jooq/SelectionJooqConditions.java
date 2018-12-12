package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class SelectionJooqConditions extends EmptyJooqFilterVisitor {
  @Override
  public void visit(CityFilter cityFilter) {
    addCondition(withUnknownCities(toCityParameters(cityFilter.values())));
  }

  @Override
  public void visit(AddressFilter addressFilter) {
    addCondition(withUnknownAddresses(toAddressParameters(addressFilter.values())));
  }

  @Override
  public void visit(OrganisationIdFilter organisationIdFilter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(organisationIdFilter.values()));
  }

  @Override
  public void visit(FacilityFilter facilityFilter) {
    if (facilityFilter.isWildcard()) {
      addCondition(LOGICAL_METER.EXTERNAL_ID.lower()
        .contains(facilityFilter.oneValue().toLowerCase()));
    } else {
      addCondition(LOGICAL_METER.EXTERNAL_ID.in(facilityFilter.values()));
    }
  }

  @Override
  public void visit(SecondaryAddressFilter secondaryAddressFilter) {
    if (secondaryAddressFilter.isWildcard()) {
      addCondition(PHYSICAL_METER.ADDRESS.lower()
        .contains(secondaryAddressFilter.oneValue().toLowerCase()));
    } else {
      addCondition(PHYSICAL_METER.ADDRESS.in(secondaryAddressFilter.values()));
    }
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    return query.leftJoin(PHYSICAL_METER)
      .on(LOGICAL_METER.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(LOGICAL_METER.ID.equal(PHYSICAL_METER.LOGICAL_METER_ID)))

      .leftJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
