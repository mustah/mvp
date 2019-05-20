package com.elvaco.mvp.core.access;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;

import static java.util.stream.Collectors.toMap;

public class SystemMeterDefinitionAccess implements SystemMeterDefinitionProvider {
  private final Map<Medium, MeterDefinition> meterDefinitionMap;

  public SystemMeterDefinitionAccess(Collection<MeterDefinition> meterDefinitions) {
    meterDefinitionMap = meterDefinitions.stream()
      .collect(toMap(
        meterDefinition -> meterDefinition.medium,
        meterDefinition -> meterDefinition
      ));
  }

  @Override
  public Optional<MeterDefinition> getByMedium(Medium medium) {
    return Optional.ofNullable(meterDefinitionMap.get(medium));
  }
}
