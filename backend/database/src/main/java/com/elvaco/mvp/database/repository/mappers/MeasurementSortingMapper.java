package com.elvaco.mvp.database.repository.mappers;

import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;

public class MeasurementSortingMapper extends SortingEntityMapper {

  private static final Map<String, String> SORTABLE_PROPERTIES = new HashMap<>();

  private static final QMeasurementEntity MEASUREMENT = QMeasurementEntity.measurementEntity;

  @Override
  public Map<String, String> getSortingMap() {
    return SORTABLE_PROPERTIES;
  }

  private static String toSortString(Object sortProperty) {
    return sortProperty.toString().replaceAll("measurementEntity.", "");
  }

  static {
    SORTABLE_PROPERTIES.put(
      "created",
      toSortString(MEASUREMENT.id.created)
    );

    SORTABLE_PROPERTIES.put(
      "quantity",
      toSortString(MEASUREMENT.id.quantity.name)
    );
  }
}
