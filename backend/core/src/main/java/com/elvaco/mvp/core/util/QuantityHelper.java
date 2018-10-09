package com.elvaco.mvp.core.util;

import java.util.Set;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.exception.UnitConversionError;

import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toSet;

@UtilityClass
public class QuantityHelper {

  public static Set<Quantity> complementWithUnits(Set<Quantity> quantities) {
    return quantities.stream()
      .map((inputQuantity) -> {
        if (inputQuantity.presentationUnit() != null) {
          return inputQuantity;
        }
        return Quantity.QUANTITIES.stream()
          .filter((knownQuantity -> knownQuantity.name.equals(inputQuantity.name)))
          .findAny()
          .orElseThrow(() -> UnitConversionError.needsUnit(inputQuantity.name));
      })
      .collect(toSet());
  }

}
