package com.elvaco.mvp.database.repository.mappers;

import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;

public class LogicalMeterSortingEntityMapper extends SortingEntityMapper {

  private static final Map<String, String> SORTABLE_PROPERTIES = new HashMap<>();

  private static final QLogicalMeterEntity LOGICAL_METER = QLogicalMeterEntity.logicalMeterEntity;

  static {
    SORTABLE_PROPERTIES.put("id", toSortString(LOGICAL_METER.id));

    SORTABLE_PROPERTIES.put("address", toSortString(LOGICAL_METER.location.streetAddress));

    SORTABLE_PROPERTIES.put("city", toSortString(LOGICAL_METER.location.city));

    SORTABLE_PROPERTIES.put(
      "manufacturer",
      toSortString(LOGICAL_METER.physicalMeters.any().manufacturer)
    );
  }

  @Override
  public Map<String, String> getSortingMap() {
    return SORTABLE_PROPERTIES;
  }

  private static String toSortString(Object sortProperty) {
    return sortProperty.toString().replaceAll("logicalMeterEntity.", "");
  }
}
