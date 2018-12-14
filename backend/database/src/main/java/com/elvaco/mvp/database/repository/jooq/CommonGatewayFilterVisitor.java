package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.FilterVisitor;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.SerialFilter;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;

interface CommonGatewayFilterVisitor extends ConditionAdding, FilterVisitor {

  @Override
  default void visit(GatewayIdFilter filter) {
    addCondition(GATEWAY.ID.equal(filter.oneValue()));
  }

  @Override
  default void visit(SerialFilter filter) {
    if (filter.isWildcard()) {
      addCondition(GATEWAY.SERIAL.containsIgnoreCase(filter.oneValue()));
    } else {
      addCondition(GATEWAY.SERIAL.in(filter.values()));
    }
  }
}
