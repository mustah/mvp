package com.elvaco.mvp.testdata;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.AlarmLogEntry.AlarmLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Gateway.GatewayBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeter.LogicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Measurement.MeasurementBuilder;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinition.MeterDefinitionBuilder;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Organisation.OrganisationBuilder;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry.StatusLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static java.util.UUID.randomUUID;

public interface ContextDsl {
  IntegrationTestFixtureContext context();

  default LogicalMeter given(LogicalMeterBuilder logicalMeter) {
    return context().given(logicalMeter);
  }

  default Collection<LogicalMeter> given(LogicalMeterBuilder... logicalMeterBuilders) {
    return context().given(logicalMeterBuilders);
  }

  /**
   * Save physical meters and connect them to the given logical meter.
   */
  default LogicalMeter given(
    LogicalMeterBuilder logicalMeter,
    PhysicalMeterBuilder... physicalMeterBuilders
  ) {
    return context().given(logicalMeter, physicalMeterBuilders);
  }

  default OrganisationWithUsers given(
    OrganisationBuilder organisationBuilder,
    UserBuilder... userBuilder
  ) {
    return context().given(organisationBuilder, userBuilder);
  }

  default Organisation given(OrganisationBuilder organisationBuilder) {
    return context().given(organisationBuilder);
  }

  default Collection<Organisation> given(OrganisationBuilder... organisationBuilders) {
    return context().given(organisationBuilders);
  }

  default LogicalMeter given(PhysicalMeterBuilder physicalMeterBuilder) {
    return context().given(physicalMeterBuilder);
  }

  default Gateway given(GatewayBuilder gateway) {
    return context().given(gateway);
  }

  default Collection<Gateway> given(GatewayBuilder... gatewayBuilders) {
    return context().given(gatewayBuilders);
  }

  default void given(StatusLogEntryBuilder... statusLogEntryBuilder) {
    context().given(statusLogEntryBuilder);
  }

  default void given(GatewayStatusLogEntryBuilderDelegate... statusLogEntryBuilder) {
    context().given(statusLogEntryBuilder);
  }

  default Collection<? extends AlarmLogEntry> given(
    AlarmLogEntryBuilder... alarmLogEntryBuilders
  ) {
    return context().given(alarmLogEntryBuilders);
  }

  default void given(MeasurementBuilder... measurementBuilders) {
    context().given(measurementBuilders);
  }

  default void given(Collection<Measurement> measurements) {
    context().given(measurements);
  }

  default MeterDefinition given(MeterDefinitionBuilder meterDefinitionBuilder) {
    return context().given(meterDefinitionBuilder);
  }

  default LogicalMeterBuilder logicalMeter() {
    return context().logicalMeter();
  }

  default UserBuilder user() {
    return context().newUser();
  }

  default PhysicalMeterBuilder physicalMeter() {
    return context().physicalMeter();
  }

  default StatusLogEntryBuilder statusLog(LogicalMeter logicalMeter) {
    return context().statusLog(logicalMeter);
  }

  default GatewayStatusLogEntryBuilderDelegate statusLog(Gateway gateway) {
    return new GatewayStatusLogEntryBuilderDelegate(context().statusLog(gateway));
  }

  default AlarmLogEntryBuilder alarm(LogicalMeter logicalMeter) {
    return context().alarm(logicalMeter);
  }

  default MeasurementBuilder measurement(LogicalMeter logicalMeter) {
    return context().measurement(logicalMeter);
  }

  default MeterDefinitionBuilder meterDefinition() {
    return context().meterDefinition();
  }

  default GatewayBuilder gateway() {
    return context().gateway();
  }

  default OrganisationBuilder organisation() {
    return context().organisation();
  }

  default OrganisationBuilder subOrganisation() {
    UUID orgId = randomUUID();
    OrganisationBuilder organisationBuilder = context().organisation().id(orgId);

    UserSelection userSelection = UserSelection.builder()
      .id(randomUUID())
      .ownerUserId(context().superAdmin.id)
      .organisationId(context().organisationId())
      .selectionParameters(toJsonNode("{}"))
      .name("user-selection for sub-organisation " + orgId.toString())
      .build();

    return organisationBuilder.parent(context().defaultOrganisation())
      .selection(userSelection);
  }

  default Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    double... values
  ) {
    return context().series(logicalMeter, quantity, values);
  }

  default Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    TemporalAmount interval,
    double... values
  ) {
    return context().series(logicalMeter, quantity, interval, values);
  }

  default Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    double... values
  ) {
    return context().series(logicalMeter, quantity, start, values);
  }

  default Collection<Measurement> series(
    LogicalMeter logicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    TemporalAmount interval,
    double... values
  ) {
    return context().series(logicalMeter, quantity, start, interval, values);
  }

  default Collection<Measurement> series(
    PhysicalMeter physicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    TemporalAmount interval,
    double... values
  ) {
    return context().series(physicalMeter, quantity, start, interval, values);
  }

  default Collection<Measurement> series(
    PhysicalMeter physicalMeter,
    Quantity quantity,
    ZonedDateTime start,
    double... values
  ) {
    return context().series(physicalMeter, quantity, start, values);
  }
}
