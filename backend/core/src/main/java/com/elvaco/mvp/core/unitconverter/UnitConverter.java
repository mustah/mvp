package com.elvaco.mvp.core.unitconverter;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;

public interface UnitConverter {

  MeasurementUnit convert(MeasurementUnit measurementUnit, String targetUnit);

  boolean isSameDimension(String firstUnit, String secondUnit);
}
