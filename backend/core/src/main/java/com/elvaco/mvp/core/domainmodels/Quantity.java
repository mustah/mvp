package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Quantity {

  public static final Quantity VOLUME = new Quantity("Volume")
    .withDefaultPresentation(
      "m³",
      SeriesDisplayMode.CONSUMPTION
    );
  public static final Quantity VOLUME_FLOW = new Quantity("Flow")
    .withDefaultPresentation(
      "m³/h",
      SeriesDisplayMode.READOUT
    );
  public static final Quantity TEMPERATURE = new Quantity("Temperature")
    .withDefaultPresentation(
      "°C",
      SeriesDisplayMode.READOUT
    );
  public static final Quantity ENERGY = new Quantity("Energy")
    .withDefaultPresentation(
      "kWh",
      SeriesDisplayMode.CONSUMPTION
    );
  public static final Quantity POWER = new Quantity("Power")
    .withDefaultPresentation(
      "W",
      SeriesDisplayMode.READOUT
    );
  public static final Quantity FORWARD_TEMPERATURE = new Quantity("Forward temperature")
    .withDefaultPresentation(
      "°C",
      SeriesDisplayMode.READOUT
    );
  public static final Quantity RETURN_TEMPERATURE = new Quantity("Return temperature")
    .withDefaultPresentation(
      "°C",
      SeriesDisplayMode.READOUT
    );
  public static final Quantity DIFFERENCE_TEMPERATURE = new Quantity(
    "Difference temperature").withDefaultPresentation(
    "K",
    SeriesDisplayMode.READOUT
  );

  private static final String QUANTITY_UNIT_DELIMITER = ":";
  public final String name;
  @Nullable
  public final Long id;
  private final QuantityPresentationInformation presentationInformation;

  public Quantity(
    @Nullable Long id,
    String name,
    QuantityPresentationInformation presentationInformation
  ) {
    this.id = id;
    this.name = name;
    this.presentationInformation = presentationInformation;
  }

  public Quantity(String name, QuantityPresentationInformation presentationInformation) {
    this(null, name, presentationInformation);
  }

  public Quantity(String name, String unit) {
    this(null, name, new QuantityPresentationInformation(unit, SeriesDisplayMode.UNKNOWN));
  }

  public Quantity(String name) {
    this(null, name, new QuantityPresentationInformation(null, SeriesDisplayMode.UNKNOWN));
  }

  public static Quantity of(String quantityUnitPair) {
    String[] parts = quantityUnitPair.split(QUANTITY_UNIT_DELIMITER);
    String quantityName = parts[0];
    if (quantityName.isEmpty() || parts.length > 2) {
      throw new RuntimeException("Invalid quantity/unit pair: '" + quantityUnitPair + "'");
    } else if (parts.length == 2) {
      return new Quantity(quantityName, parts[1]);
    } else {
      return new Quantity(quantityName);
    }
  }

  @Nullable
  public String presentationUnit() {
    return presentationInformation.getUnit().orElse(null);
  }

  public Quantity withUnit(String unit) {
    return new Quantity(id, name, presentationInformation.withUnit(unit));
  }

  public SeriesDisplayMode seriesDisplayMode() {
    return presentationInformation.getSeriesDisplayMode();
  }

  public Quantity withSeriesDisplayMode(SeriesDisplayMode seriesDisplayMode) {
    return new Quantity(id, name, presentationInformation.withSeriesDisplayMode(seriesDisplayMode));
  }

  private Quantity withDefaultPresentation(String unit, SeriesDisplayMode displayMode) {
    return new Quantity(
      id,
      name,
      new QuantityPresentationInformation(unit, displayMode)
    );
  }
}
