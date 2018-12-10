package com.elvaco.mvp.database.repository.mappers;

import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;

public class LogicalMeterSortingEntityMapper extends SortingEntityMapper {

  private static final Map<String, ComparableExpressionBase<?>> SORTABLE_PROPERTIES =
    new HashMap<>();

  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  private static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;

  static {
    SORTABLE_PROPERTIES.put("id", LOGICAL_METER.pk.id);

    SORTABLE_PROPERTIES.put("address", LOGICAL_METER.location.streetAddress);

    SORTABLE_PROPERTIES.put("secondaryAddress", PHYSICAL_METER.address);

    SORTABLE_PROPERTIES.put("externalId", PHYSICAL_METER.externalId);

    SORTABLE_PROPERTIES.put("city", LOGICAL_METER.location.city);

    SORTABLE_PROPERTIES.put("manufacturer", PHYSICAL_METER.manufacturer);
  }

  @Override
  public Map<String, ComparableExpressionBase<?>> getSortingMap() {
    return SORTABLE_PROPERTIES;
  }
}
