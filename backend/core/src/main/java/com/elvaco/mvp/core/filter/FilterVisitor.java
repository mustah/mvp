package com.elvaco.mvp.core.filter;

public interface FilterVisitor {

  void visit(CityFilter filter);

  void visit(AddressFilter filter);

  void visit(OrganisationIdFilter filter);

  void visit(GatewayIdFilter filter);

  void visit(AlarmFilter filter);

  void visit(ReportPeriodFilter filter);

  void visit(ThresholdPeriodFilter filter);

  void visit(CollectionPeriodFilter filter);

  void visit(SerialFilter filter);

  void visit(WildcardFilter filter);

  void visit(LocationConfidenceFilter filter);

  void visit(MeterStatusFilter filter);

  void visit(MediumFilter filter);

  void visit(FacilityFilter filter);

  void visit(SecondaryAddressFilter filter);

  void visit(ManufacturerFilter filter);

  void visit(LogicalMeterIdFilter filter);

  void visit(MeasurementThresholdFilter filter);

  void visit(OrganisationParentFilter organisationParentFilter);
}
