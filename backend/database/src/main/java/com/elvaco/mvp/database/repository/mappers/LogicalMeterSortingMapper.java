package com.elvaco.mvp.database.repository.mappers;

import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;

public class LogicalMeterSortingMapper extends SortingMapper {

  private static final Map<String, String> SORTABLE_PROPERTIES = new HashMap<>();

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  static {
    SORTABLE_PROPERTIES.put("id", toSortString(Q.id));

    SORTABLE_PROPERTIES.put("address", toSortString(Q.location.streetAddress));

    SORTABLE_PROPERTIES.put("city", toSortString(Q.location.city));

    SORTABLE_PROPERTIES.put("manufacturer", toSortString(Q.physicalMeters.any().manufacturer));
  }

  @Override
  public Map<String, String> getSortingMap() {
    return SORTABLE_PROPERTIES;
  }

  private static String toSortString(Object sortProperty) {
    return sortProperty.toString().replaceAll("logicalMeterEntity.", "");
  }
}
