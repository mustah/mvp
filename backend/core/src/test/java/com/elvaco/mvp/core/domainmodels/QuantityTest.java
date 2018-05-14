package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuantityTest {

  @Test
  public void quantityAndUnitAreParsedCorrectly() {
    assertThat(Quantity.of("Energy:kWh"))
      .isEqualTo(new Quantity("Energy", "kWh"));

    assertThat(Quantity.of("Power:MW"))
      .isEqualTo(new Quantity("Power", "MW"));
  }

  @Test
  public void parsingTooManyElementsThrowsException() {
    assertThatThrownBy(() -> Quantity.of("A:B:C"))
      .hasMessageContaining("Invalid quantity/unit pair");
  }

  @Test
  public void parsingEmptyUnitIsOkay() {
    assertThat(Quantity.of("Energy:"))
      .isEqualTo(new Quantity("Energy"));
  }

  @Test
  public void parsingMissingQuantityThrowsException() {
    assertThatThrownBy(() -> Quantity.of(":A"))
      .hasMessageContaining("Invalid quantity/unit pair");
  }

  @Test
  public void parsingEmptyInputThrowsException() {
    assertThatThrownBy(() -> Quantity.of(""))
      .hasMessageContaining("Invalid quantity/unit pair");
  }

  @Test
  public void parsingOnlyQuantityIsOkay() {
    assertThat(Quantity.of("Energy"))
      .isEqualTo(new Quantity("Energy"));
  }

  @Test
  public void complementComplete() {
    assertThat(Quantity.POWER.complementedBy(
      new QuantityPresentationInformation("kW", SeriesDisplayMode.READOUT)
    )).isEqualTo(Quantity.POWER);
  }

  @Test
  public void complementMissingUnit() {
    Quantity quantity = new Quantity(
      "a",
      new QuantityPresentationInformation(null, SeriesDisplayMode.READOUT)
    );
    assertThat(quantity.complementedBy(
      new QuantityPresentationInformation("kW", SeriesDisplayMode.READOUT)
    )).isEqualTo(
      new Quantity("a", new QuantityPresentationInformation("kW", SeriesDisplayMode.READOUT))
    );
  }

  @Test
  public void complementUnknownMode() {
    Quantity quantity = new Quantity(
      "a",
      new QuantityPresentationInformation("W", SeriesDisplayMode.UNKNOWN)
    );
    assertThat(quantity.complementedBy(
      new QuantityPresentationInformation("kW", SeriesDisplayMode.READOUT)
    )).isEqualTo(
      new Quantity("a", new QuantityPresentationInformation("W", SeriesDisplayMode.READOUT))
    );
  }

  @Test
  public void complementMissingUnitWithNoUnitRaisesException() {
    Quantity quantity = new Quantity(
      "a",
      new QuantityPresentationInformation(null, SeriesDisplayMode.UNKNOWN)
    );
    assertThatThrownBy(() -> quantity.complementedBy(
      new QuantityPresentationInformation(null, SeriesDisplayMode.READOUT)
    )).isInstanceOf(IllegalArgumentException.class);
  }
}
