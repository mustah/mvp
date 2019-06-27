package com.elvaco.mvp.core.util;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CompletenessValidators {

  public static final CompletenessValidator<LogicalMeter> LOGICAL_METER_VALIDATOR =
    new CompletenessValidator<>(
      logicalMeter -> logicalMeter.location.isKnown(),
      logicalMeter -> !logicalMeter.meterDefinition.equals(MeterDefinition.UNKNOWN)
    );

  public static final CompletenessValidator<PhysicalMeter> PHYSICAL_METER_VALIDATOR =
    new CompletenessValidator<>(
      physicalMeter -> !physicalMeter.medium.equals(Medium.UNKNOWN_MEDIUM),
      physicalMeter -> physicalMeter.manufacturer != null
        && !physicalMeter.manufacturer.equals("UNKNOWN"),
      physicalMeter -> physicalMeter.readIntervalMinutes != 0,
      physicalMeter -> physicalMeter.revision != null
    );

  public static final CompletenessValidator<Gateway> GATEWAY_VALIDATOR =
    new CompletenessValidator<>(gateway -> !gateway.productModel.isEmpty());
}
