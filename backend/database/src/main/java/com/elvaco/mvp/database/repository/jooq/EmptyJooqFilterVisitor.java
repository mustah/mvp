package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.ManufacturerFilter;
import com.elvaco.mvp.core.filter.MediumFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;
import com.elvaco.mvp.core.filter.SerialFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

abstract class EmptyJooqFilterVisitor extends JooqFilterVisitor {

  @Override
  public void visit(CityFilter cityFilter) {}

  @Override
  public void visit(AddressFilter addressFilter) {}

  @Override
  public void visit(OrganisationIdFilter organisationIdFilter) {}

  @Override
  public void visit(GatewayIdFilter gatewayIdFilter) {}

  @Override
  public void visit(AlarmFilter alarmFilter) {}

  @Override
  public void visit(PeriodFilter periodFilter) {}

  @Override
  public void visit(SerialFilter serialFilter) {}

  @Override
  public void visit(WildcardFilter wildcardFilter) {}

  @Override
  public void visit(LocationConfidenceFilter locationConfidenceFilter) {}

  @Override
  public void visit(MeterStatusFilter meterStatusFilter) {}

  @Override
  public void visit(MediumFilter mediumFilter) {}

  @Override
  public void visit(FacilityFilter facilityFilter) {}

  @Override
  public void visit(SecondaryAddressFilter secondaryAddressFilter) {}

  @Override
  public void visit(ManufacturerFilter manufacturerFilter) {}

  @Override
  public void visit(LogicalMeterIdFilter logicalMeterIdFilter) {}
}
