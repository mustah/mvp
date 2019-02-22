package com.elvaco.mvp.database.repository.jooq;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  public static FilterAcceptor organisation() {
    return new OrganisationFilterVisitor();
  }

  public static FilterAcceptor logicalMeter(DSLContext dsl, MeasurementThresholdParser parser) {
    return new LogicalMeterFilterVisitor(filterDecorators(dsl, parser));
  }

  public static FilterAcceptor logicalMeterWithCollectionPercentage(
    DSLContext dsl,
    MeasurementThresholdParser parser
  ) {
    return new LogicalMeterFilterVisitor(Stream.concat(
      filterDecorators(dsl, parser).stream(),
      Stream.of(new CollectionPercentageFilterVisitor(dsl))
    ).collect(Collectors.toList()));
  }

  public static FilterAcceptor logicalMeterWithCollectionPercentageAndLastData(
    DSLContext dsl,
    MeasurementThresholdParser parser
  ) {
    return new LogicalMeterFilterVisitor(Stream.concat(
      filterDecorators(dsl, parser).stream(),
      Stream.of(new CollectionPercentageFilterVisitor(dsl),
        new MeasurementLastDataFilterVisitor(dsl))
    ).collect(Collectors.toList()));
  }

  public static FilterAcceptor collectionPercentagePerDate(
    DSLContext dsl,
    MeasurementThresholdParser parser
  ) {
    return new LogicalMeterFilterVisitor(Stream.concat(
      filterDecorators(dsl, parser).stream(),
      Stream.of(new CollectionPercentagePerDateFilterVisitor(dsl))
    ).collect(Collectors.toList()));
  }

  public static FilterAcceptor gateway(DSLContext dsl, MeasurementThresholdParser parser) {
    return new GatewayFilterVisitor(dsl, filterDecorators(dsl, parser));
  }

  private static List<FilterAcceptor> filterDecorators(
    DSLContext dsl,
    MeasurementThresholdParser parser
  ) {
    return List.of(
      new MeasurementStatsFilterVisitor(parser),
      new MeterAlarmLogFilterVisitor(dsl),
      new PhysicalMeterStatusLogFilterVisitor(dsl)
    );
  }
}
