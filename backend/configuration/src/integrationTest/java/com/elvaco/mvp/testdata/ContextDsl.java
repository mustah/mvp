package com.elvaco.mvp.testdata;

import java.util.Collection;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.AlarmLogEntry.AlarmLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.core.domainmodels.Dashboard.DashboardBuilder;
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
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry.StatusLogEntryBuilder;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.core.domainmodels.Widget.WidgetBuilder;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static java.util.UUID.randomUUID;

public interface ContextDsl {
  IntegrationTestFixtureContext context();

  default LogicalMeter given(LogicalMeterBuilder logicalMeter) {
    return context().given(logicalMeter, true);
  }

  default LogicalMeter given(LogicalMeterBuilder logicalMeter, boolean withConnectedPhysicalMeter) {
    return context().given(logicalMeter, withConnectedPhysicalMeter);
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

  default User given(UserBuilder userBuilder) {
    return context().given(userBuilder);
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

  default void given(LogicalMeter logicalMeter, MeasurementBuilder... measurementBuilders) {
    context().given(logicalMeter, measurementBuilders);
  }

  default void given(LogicalMeter logicalMeter, Collection<Measurement> measurements) {
    context().given(measurements, logicalMeter);
  }

  default MeterDefinition given(MeterDefinitionBuilder meterDefinitionBuilder) {
    return context().given(meterDefinitionBuilder);
  }

  default Dashboard given(DashboardBuilder dashboardBuilder) {
    return context().given(dashboardBuilder);
  }

  default Collection<Measurement> given(MeasurementSeriesBuilder seriesBuilder) {
    return context().given(seriesBuilder);
  }

  default Widget given(WidgetBuilder widgetBuilder) {
    return context().given(widgetBuilder);
  }

  default LogicalMeterBuilder logicalMeter() {
    return context().logicalMeter();
  }

  default DashboardBuilder dashboard() {
    return context().dashboard();
  }

  default WidgetBuilder widget() {
    return context().widget();
  }

  default UserBuilder mvpUser() {
    return context().newMvpUser();
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
    return subOrganisation(context().organisation, context().superAdmin);
  }

  default OrganisationBuilder subOrganisation(Organisation parent, User owner) {
    UUID subOrganisationId = randomUUID();

    return organisation()
      .id(subOrganisationId)
      .parent(parent)
      .selection(
        UserSelection.builder()
          .id(randomUUID())
          .ownerUserId(owner.id)
          .organisationId(parent.id)
          .selectionParameters(toJsonNode("{}"))
          .name("user-selection for sub-organisation " + subOrganisationId.toString())
          .build()
      );
  }

  default MeasurementSeriesBuilder measurementSeries() {
    return new MeasurementSeriesBuilder();
  }
}
