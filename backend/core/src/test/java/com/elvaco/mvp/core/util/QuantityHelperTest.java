package com.elvaco.mvp.core.util;

import java.util.HashSet;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.Quantity;

import org.junit.Test;

import static com.elvaco.mvp.core.util.QuantityHelper.complementWithUnits;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuantityHelperTest {

  @Test
  public void complementWithUnits_defaultsToKnownUnit() {
    Quantity customQuantity = new Quantity("Volume");
    Set<Quantity> quantities = complementWithUnits(asdf(customQuantity));
    assertThat(quantities)
      .containsExactly(Quantity.VOLUME);
  }

  @Test
  public void complementWithUnits_unknownQuantityWithoutUnitThrowsException() {
    Quantity customQuantity = new Quantity("Flabber");
    assertThatThrownBy(() -> complementWithUnits(asdf(customQuantity)))
      .hasMessageContaining("Flabber needs to be complemented with a unit");
  }

  @Test
  public void complementWithUnits_customQuantityIsOkWhenAccompaniedByUnit() {
    Quantity customQuantity = new Quantity("Flabber", "Mandroids");
    Set<Quantity> quantities = complementWithUnits(asdf(customQuantity));
    assertThat(quantities).containsExactly(customQuantity);
  }

  private Set<Quantity> asdf(Quantity quantity) {
    return new HashSet<>(asList(quantity));
  }

}
