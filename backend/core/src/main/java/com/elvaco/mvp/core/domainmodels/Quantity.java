package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@EqualsAndHashCode
@ToString
public class Quantity implements Identifiable<Integer>, Serializable {

  public static final Quantity VOLUME = new Quantity("Volume")
    .withDefaultPresentation("m³", SeriesDisplayMode.CONSUMPTION);

  public static final Quantity VOLUME_FLOW = new Quantity("Flow")
    .withDefaultPresentation("m³/h", SeriesDisplayMode.READOUT);

  public static final Quantity TEMPERATURE = new Quantity("Temperature")
    .withDefaultPresentation("°C", SeriesDisplayMode.READOUT);

  public static final Quantity HUMIDITY = new Quantity("Relative humidity")
    .withDefaultPresentation("%", SeriesDisplayMode.READOUT);

  public static final Quantity ENERGY = new Quantity("Energy")
    .withDefaultPresentation("kWh", SeriesDisplayMode.CONSUMPTION);

  public static final Quantity POWER = new Quantity("Power")
    .withDefaultPresentation("W", SeriesDisplayMode.READOUT);

  public static final Quantity FORWARD_TEMPERATURE = new Quantity("Forward temperature")
    .withDefaultPresentation("°C", SeriesDisplayMode.READOUT);

  public static final Quantity RETURN_TEMPERATURE = new Quantity("Return temperature")
    .withDefaultPresentation("°C", SeriesDisplayMode.READOUT);

  public static final Quantity DIFFERENCE_TEMPERATURE = new Quantity("Difference temperature")
    .withDefaultPresentation("K", SeriesDisplayMode.READOUT);

  public static final Quantity ENERGY_RETURN = new Quantity("Energy return")
    .withDefaultPresentation("kWh", SeriesDisplayMode.CONSUMPTION);

  public static final Quantity REACTIVE_ENERGY = new Quantity("Reactive energy")
    .withDefaultPresentation("kWh", SeriesDisplayMode.CONSUMPTION);

  public static final List<Quantity> QUANTITIES = unmodifiableList(asList(
    VOLUME,
    VOLUME_FLOW,
    TEMPERATURE,
    HUMIDITY,
    ENERGY,
    POWER,
    FORWARD_TEMPERATURE,
    RETURN_TEMPERATURE,
    DIFFERENCE_TEMPERATURE,
    ENERGY_RETURN,
    REACTIVE_ENERGY
  ));

  private static final long serialVersionUID = -4279706519680318521L;

  private static final String QUANTITY_UNIT_DELIMITER = ":";

  public final String name;

  @Nullable
  public final Integer id;
  private final QuantityPresentationInformation presentationInformation;

  public Quantity(
    @Nullable Integer id,
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

  public Quantity(String name, @Nullable String unit) {
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

  public QuantityPresentationInformation getPresentationInformation() {
    return presentationInformation;
  }

  @Nullable
  public String presentationUnit() {
    return presentationInformation.getUnit().orElse(null);
  }

  public SeriesDisplayMode seriesDisplayMode() {
    return presentationInformation.displayMode;
  }

  public Quantity complementedBy(QuantityPresentationInformation presentationInformation) {
    return new Quantity(
      name,
      new QuantityPresentationInformation(
        this.presentationInformation.getUnit()
          .orElse(presentationInformation.getUnit().orElseThrow(IllegalArgumentException::new)),
        seriesDisplayMode() == SeriesDisplayMode.UNKNOWN
          ? presentationInformation.displayMode
          : seriesDisplayMode()
      )
    );
  }

  @Nullable
  @Override
  public Integer getId() {
    return id;
  }

  private Quantity withDefaultPresentation(String unit, SeriesDisplayMode displayMode) {
    return new Quantity(
      id,
      name,
      new QuantityPresentationInformation(unit, displayMode)
    );
  }
}
