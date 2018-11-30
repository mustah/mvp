package com.elvaco.mvp.core.filter;

import java.util.Collection;
import java.util.UUID;

public class GatewayIdFilter extends IdFilter {
  protected GatewayIdFilter(
    Collection<UUID> values
  ) {
    super(values);
  }

  @Override
  public void accept(FilterVisitor visitor) {
    visitor.visit(this);
  }
}
