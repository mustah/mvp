package com.elvaco.mvp.database.repository.jooq;

import java.util.List;

import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.CollectionPeriodFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.ManufacturerFilter;
import com.elvaco.mvp.core.filter.MeasurementThresholdFilter;
import com.elvaco.mvp.core.filter.MediumFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.OrganisationParentFilter;
import com.elvaco.mvp.core.filter.ReportPeriodFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;
import com.elvaco.mvp.core.filter.SerialFilter;
import com.elvaco.mvp.core.filter.ThresholdPeriodFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

abstract class EmptyFilterVisitor extends JooqFilterVisitor {

  EmptyFilterVisitor() {
    super(List.of());
  }

  @Override
  public void visit(CityFilter filter) {}

  @Override
  public void visit(AddressFilter filter) {}

  @Override
  public void visit(OrganisationIdFilter filter) {}

  @Override
  public void visit(OrganisationParentFilter filter) {}

  @Override
  public void visit(GatewayIdFilter filter) {}

  @Override
  public void visit(AlarmFilter filter) {}

  @Override
  public void visit(ReportPeriodFilter filter) {}

  @Override
  public void visit(ThresholdPeriodFilter filter) {}

  @Override
  public void visit(CollectionPeriodFilter filter) { }

  @Override
  public void visit(SerialFilter filter) {}

  @Override
  public void visit(WildcardFilter filter) {}

  @Override
  public void visit(LocationConfidenceFilter filter) {}

  @Override
  public void visit(MeterStatusFilter filter) {}

  @Override
  public void visit(MediumFilter filter) {}

  @Override
  public void visit(FacilityFilter filter) {}

  @Override
  public void visit(SecondaryAddressFilter filter) {}

  @Override
  public void visit(ManufacturerFilter filter) {}

  @Override
  public void visit(LogicalMeterIdFilter filter) {}

  @Override
  public void visit(MeasurementThresholdFilter filter) {}
}
