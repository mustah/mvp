package com.elvaco.mvp.core.unitconverter;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;

public interface UnitConverter {

  MeasurementUnit toMeasurementUnit(String valueAndUnit, String targetUnit);

  MeasurementUnit toMeasurementUnit(MeasurementUnit measurementUnit, String targetUnit);

  boolean isSameDimension(String firstUnit, String secondUnit);

  double toValue(double value, String fromUnit, String toUnit);

}
