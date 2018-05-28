package com.elvaco.mvp.core.util;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CompletenessValidators {

  private static final CompletenessValidator<LogicalMeter> LOGICAL_METER_COMPLETENESS_VALIDATOR =
    new CompletenessValidator<>(
      logicalMeter -> logicalMeter.location.isKnown(),
      logicalMeter -> !logicalMeter.meterDefinition.equals(MeterDefinition.UNKNOWN_METER)
    );

  private static final CompletenessValidator<PhysicalMeter> PHYSICAL_METER_COMPLETENESS_VALIDATOR =
    new CompletenessValidator<>(
      physicalMeter -> !physicalMeter.medium.equals(Medium.UNKNOWN_MEDIUM.medium),
      physicalMeter -> physicalMeter.manufacturer != null
        && !physicalMeter.manufacturer.equals("UNKNOWN")
    );

  private static final CompletenessValidator<Gateway> GATEWAY_COMPLETENESS_VALIDATOR =
    new CompletenessValidator<>(
      gateway -> !gateway.productModel.isEmpty()
    );

  public static CompletenessValidator<LogicalMeter> logicalMeter() {
    return LOGICAL_METER_COMPLETENESS_VALIDATOR;
  }

  public static CompletenessValidator<PhysicalMeter> physicalMeter() {
    return PHYSICAL_METER_COMPLETENESS_VALIDATOR;
  }

  public static CompletenessValidator<Gateway> gateway() {
    return GATEWAY_COMPLETENESS_VALIDATOR;
  }

}