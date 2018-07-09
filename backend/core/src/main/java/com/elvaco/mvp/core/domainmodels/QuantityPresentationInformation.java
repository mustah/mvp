package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class QuantityPresentationInformation implements Serializable {

  private static final long serialVersionUID = -3928748851741551018L;

  @Nullable
  private final String unit;
  public final SeriesDisplayMode displayMode;

  public Optional<String> getUnit() {
    return Optional.ofNullable(unit);
  }
}
