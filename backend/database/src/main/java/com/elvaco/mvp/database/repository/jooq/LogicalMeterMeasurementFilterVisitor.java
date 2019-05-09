package com.elvaco.mvp.database.repository.jooq;

import java.util.Collection;

import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEDIUM;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

class LogicalMeterMeasurementFilterVisitor extends CommonFilterVisitor {

  LogicalMeterMeasurementFilterVisitor(Collection<FilterAcceptor> decorators) {
    super(decorators);
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(WildcardFilter filter) {}

  @Override
  public void visit(LogicalMeterIdFilter filter) {
    addCondition(LOGICAL_METER.ID.in(filter.values()));
  }

  @Override
  public void visit(CityFilter filter) {
    addCondition(withUnknownCities(toCityParameters(filter.values())));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(PHYSICAL_METER)
      .on(
        PHYSICAL_METER.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
          .and(PHYSICAL_METER.LOGICAL_METER_ID.equal(LOGICAL_METER.ID))
      )

      .innerJoin(METER_DEFINITION)
      .on(METER_DEFINITION.ID.equal(LOGICAL_METER.METER_DEFINITION_ID))

      .innerJoin(MEDIUM)
      .on(MEDIUM.ID.equal(METER_DEFINITION.MEDIUM_ID))

      .innerJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
