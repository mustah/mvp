package com.elvaco.mvp.database.repository.jooq;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEDIUM;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.asterisk;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.noCondition;
import static org.jooq.impl.DSL.rowNumber;
import static org.jooq.impl.DSL.select;

/**
 * Include all logical meters with all active and inactive physical meters.
 */
class LogicalMeterMeasurementFilterVisitor extends CommonFilterVisitor {

  private static final String ACTIVE_GATEWAY_METERS = "ACTIVE_GATEWAY_METERS";
  private static final Field<UUID> ACTIVE_GATEWAYS_METERS_ORGANISATION_ID = field(
    ACTIVE_GATEWAY_METERS + "." + GATEWAYS_METERS.ORGANISATION_ID.getName(),
    GATEWAYS_METERS.ORGANISATION_ID.getType()
  );
  private static final Field<UUID> ACTIVE_GATEWAYS_METERS_GATEWAY_ID = field(
    ACTIVE_GATEWAY_METERS + "." + GATEWAYS_METERS.GATEWAY_ID.getName(),
    GATEWAYS_METERS.GATEWAY_ID.getType()
  );
  private static final Field<UUID> ACTIVE_GATEWAYS_METERS_LOGICAL_METER_ID = field(
    ACTIVE_GATEWAY_METERS + "." + GATEWAYS_METERS.LOGICAL_METER_ID.getName(),
    GATEWAYS_METERS.LOGICAL_METER_ID.getType()
  );

  LogicalMeterMeasurementFilterVisitor(Collection<FilterAcceptor> decorators) {
    super(decorators);
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(WildcardFilter filter) {
    String value = filter.oneValue().toLowerCase();

    addCondition(LOGICAL_METER.EXTERNAL_ID.lower().contains(value)
      .or(MEDIUM.NAME.lower().contains(value))
      .or(LOCATION.CITY.lower().contains(value))
      .or(LOCATION.STREET_ADDRESS.lower().contains(value))
      .or(PHYSICAL_METER.MANUFACTURER.lower().contains(value))
      .or(PHYSICAL_METER.ADDRESS.lower().contains(value)));
  }

  Condition physicalMeterPeriodCondition() {
    return noCondition();
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    List<SelectField<?>> gatewaysMetersFields = GATEWAYS_METERS.fieldStream().collect(toList());
    Field<Integer> rowNumber = rowNumber().over()
      .partitionBy(GATEWAYS_METERS.ORGANISATION_ID, GATEWAYS_METERS.LOGICAL_METER_ID)
      .orderBy(GATEWAYS_METERS.LAST_SEEN.desc().nullsLast()).as("row_number");
    gatewaysMetersFields.add(rowNumber);

    return
      query.leftJoin(PHYSICAL_METER)
        .on(
          PHYSICAL_METER.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
            .and(PHYSICAL_METER.LOGICAL_METER_ID.equal(LOGICAL_METER.ID))
            .and(physicalMeterPeriodCondition())
        )

        .leftJoin((lateral(
          select(asterisk())
            .from(
              select(gatewaysMetersFields)
                .from(GATEWAYS_METERS).where(GATEWAYS_METERS.LOGICAL_METER_ID
                .eq(PHYSICAL_METER.LOGICAL_METER_ID))
            ).where(rowNumber.eq(1)).asTable(ACTIVE_GATEWAY_METERS)
        )))
        .on(ACTIVE_GATEWAYS_METERS_ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
          .and(ACTIVE_GATEWAYS_METERS_LOGICAL_METER_ID.equal(LOGICAL_METER.ID)))

        .leftJoin(GATEWAY)
        .on(GATEWAY.ORGANISATION_ID.equal(ACTIVE_GATEWAYS_METERS_ORGANISATION_ID)
          .and(GATEWAY.ID.equal(ACTIVE_GATEWAYS_METERS_GATEWAY_ID)))

        .innerJoin(METER_DEFINITION)
        .on(METER_DEFINITION.ID.equal(LOGICAL_METER.METER_DEFINITION_ID))

        .innerJoin(MEDIUM)
        .on(MEDIUM.ID.equal(METER_DEFINITION.MEDIUM_ID))

        /*This inner join is fine - a meter always has a location entry, although it might be NULL.
         * While a left join would be more "semantically" correct, this allows us to perform quick
         * sorting on location columns */
        .innerJoin(LOCATION)
        .on(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
          .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)));
  }
}
