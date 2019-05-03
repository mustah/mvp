package com.elvaco.mvp.database.repository.jooq;

import java.util.Collection;

import com.elvaco.mvp.core.filter.CollectionPeriodFilter;
import com.elvaco.mvp.core.filter.MeasurementThresholdFilter;
import com.elvaco.mvp.core.filter.OrganisationParentFilter;

abstract class CommonFilterVisitor
  extends JooqFilterVisitor
  implements CommonLocationFilterVisitor,
             CommonGatewayFilterVisitor,
             CommonLogicalMeterFilterVisitor,
             PhysicalMeterFilterVisitor {

  CommonFilterVisitor(Collection<FilterAcceptor> decorators) {
    super(decorators);
  }

  @Override
  public void visit(MeasurementThresholdFilter filter) {
  }

  @Override
  public void visit(OrganisationParentFilter filter) {
  }

  @Override
  public void visit(CollectionPeriodFilter filter) { }
}
