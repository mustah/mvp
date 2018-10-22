package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class QuantityPresentationInformation {

  @Nullable
  private final String unit;
  public final SeriesDisplayMode displayMode;

  public Optional<String> getUnit() {
    return Optional.ofNullable(unit);
  }
}
