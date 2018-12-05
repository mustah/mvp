package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.SerialFilter;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;

public interface GatewayFilterVisitor extends ConditionAdding, FilterVisitor {

  @Override
  default void visit(GatewayIdFilter gatewayIdFilter) {
    addCondition(GATEWAY.ID.equal(gatewayIdFilter.oneValue()));
  }

  @Override
  default void visit(SerialFilter serialFilter) {
    if (serialFilter.isWildcard()) {
      addCondition(GATEWAY.SERIAL.containsIgnoreCase(serialFilter.oneValue()));
    } else {
      addCondition(GATEWAY.SERIAL.in(serialFilter.values()));
    }
  }
}
