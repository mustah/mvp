package com.elvaco.mvp.core.access;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.exception.NoSuchMeterDefinition;

public interface SystemMeterDefinitionProvider {
  Optional<MeterDefinition> getByMedium(Medium medium);

  default MeterDefinition getByMediumOrThrow(Medium medium) {
    return getByMedium(medium).orElseThrow(() -> new NoSuchMeterDefinition(medium));
  }
}
