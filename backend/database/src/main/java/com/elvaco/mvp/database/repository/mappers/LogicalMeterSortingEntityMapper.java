package com.elvaco.mvp.database.repository.mappers;

import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;

import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;

public class LogicalMeterSortingEntityMapper extends SortingEntityMapper {

  private static final Map<String, String> SORTABLE_PROPERTIES = new HashMap<>();

  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;

  static {
    SORTABLE_PROPERTIES.put("id", toSortString(LOGICAL_METER.id));

    SORTABLE_PROPERTIES.put("address", toSortString(LOGICAL_METER.location.streetAddress));

    SORTABLE_PROPERTIES.put("city", toSortString(LOGICAL_METER.location.city));

    SORTABLE_PROPERTIES.put(
      "manufacturer",
      toSortString(PHYSICAL_METER.manufacturer)
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
