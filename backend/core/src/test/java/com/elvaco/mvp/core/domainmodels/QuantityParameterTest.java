package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuantityParameterTest {

  @Test
  public void quantityAndUnitAreParsedCorrectly() {
    assertThat(QuantityParameter.of("Energy:kWh")).isEqualTo(new QuantityParameter(
      "Energy",
      "kWh"
    ));

    assertThat(QuantityParameter.of("Power:MW")).isEqualTo(new QuantityParameter("Power", "MW"));
  }

  @Test
  public void quantityUnitAndDispplayModeAreParsedCorrectly() {
    assertThat(QuantityParameter.of("Energy:kWh:readout")).isEqualTo(new QuantityParameter(
      "Energy",
      "kWh",
      DisplayMode.READOUT
    ));
  }

  @Test
  public void parsingInvalidDisplayModeDefaultsToUnknown() {
    assertThat(QuantityParameter.of("Energy:kWh:asdf")).isEqualTo(new QuantityParameter(
      "Energy",
      "kWh",
      DisplayMode.UNKNOWN
    ));
  }

  @Test
  public void parsingTooManyElementsThrowsException() {
    assertThatThrownBy(() -> QuantityParameter.of("A:B:C:D"))
      .hasMessageContaining("Invalid quantity/unit pair/display mode");
  }

  @Test
  public void parsingEmptyUnitIsOkay() {
    assertThat(QuantityParameter.of("Energy:")).isEqualTo(new QuantityParameter("Energy"));
  }

  @Test
  public void parsingMissingQuantityThrowsException() {
    assertThatThrownBy(() -> QuantityParameter.of(":A")).hasMessageContaining(
      "Invalid quantity/unit pair");
  }

  @Test
  public void parsingEmptyInputThrowsException() {
    assertThatThrownBy(() -> QuantityParameter.of("")).hasMessageContaining(
      "Invalid quantity/unit pair");
  }

  @Test
  public void parsingOnlyQuantityIsOkay() {
    assertThat(QuantityParameter.of("Energy"))
      .isEqualTo(new QuantityParameter("Energy"));
  }

  @Test
  public void quantityDisplayModeIsOkay() {
    assertThat(QuantityParameter.of("Energy::readout")).isEqualTo(new QuantityParameter(
      "Energy",
      null,
      DisplayMode.READOUT
    ));
  }
}
