package com.elvaco.mvp.core.filter;

public interface FilterVisitor {
  void visit(StatusTypeSelectionPeriodFilter statusTypeSelectionPeriodFilter);

  void visit(CityFilter cityFilter);

  void visit(AddressFilter addressFilter);

  void visit(OrganisationIdFilter organisationIdFilter);

  void visit(GatewayIdFilter gatewayIdFilter);

  void visit(AlarmFilter alarmFilter);

  void visit(PeriodFilter periodFilter);

  void visit(SerialFilter serialFilter);

  void visit(WildcardFilter wildcardFilter);

  void visit(LocationConfidenceFilter locationConfidenceFilter);
}
