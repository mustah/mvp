package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class QuantityPresentationInformation {

  @Nullable
  private final String unit;
  private final SeriesDisplayMode displayMode;

  public QuantityPresentationInformation(
    @Nullable String unit,
    SeriesDisplayMode displayMode
  ) {
    this.unit = unit;
    this.displayMode = displayMode;
  }

  public SeriesDisplayMode getSeriesDisplayMode() {
    return displayMode;
  }

  public Optional<String> getUnit() {
    return Optional.ofNullable(unit);
  }

  QuantityPresentationInformation withUnit(String unit) {
    return new QuantityPresentationInformation(unit, displayMode);
  }

  QuantityPresentationInformation withSeriesDisplayMode(
    SeriesDisplayMode
      seriesDisplayMode
  ) {
    return new QuantityPresentationInformation(unit, seriesDisplayMode);
  }
}
