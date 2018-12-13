package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.util.MeasurementThresholdParser;

import lombok.experimental.UtilityClass;
import org.jooq.DSLContext;

@UtilityClass
public class FilterVisitors {

  public static FilterAcceptor location() {
    return new LocationFilterVisitor();
  }

  public static FilterAcceptor measurement() {
    return new MeasurementFilterVisitor();
  }

  public static FilterAcceptor selection() {
    return new SelectionFilterVisitor();
  }

  public static FilterAcceptor logicalMeter(
    DSLContext dsl,
    MeasurementThresholdParser thresholdParser
  ) {
    return new LogicalMeterFilterVisitor(dsl, thresholdParser);
  }

  public static FilterAcceptor gateway(
    DSLContext dsl,
    MeasurementThresholdParser thresholdParser
  ) {
    return new GatewayFilterVisitor(dsl, thresholdParser);
  }
}
