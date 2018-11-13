package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@EqualsAndHashCode
@ToString
public class Quantity implements Identifiable<Integer> {

  public static final Quantity VOLUME = new Quantity("Volume")
    .withUnits("m³", SeriesDisplayMode.CONSUMPTION);

  public static final Quantity VOLUME_FLOW = new Quantity("Flow")
    .withUnits("m³/h", SeriesDisplayMode.READOUT);

  public static final Quantity TEMPERATURE = new Quantity("Temperature")
    .withUnits("°C", SeriesDisplayMode.READOUT);

  public static final Quantity EXTERNAL_TEMPERATURE = new Quantity("External temperature")
    .withUnits("°C", SeriesDisplayMode.READOUT);

  public static final Quantity HUMIDITY = new Quantity("Relative humidity")
    .withUnits("%", SeriesDisplayMode.READOUT);

  public static final Quantity ENERGY = new Quantity("Energy")
    .withUnits("kWh", SeriesDisplayMode.CONSUMPTION);

  public static final Quantity POWER = new Quantity("Power")
    .withUnits("W", SeriesDisplayMode.READOUT);

  public static final Quantity FORWARD_TEMPERATURE = new Quantity("Forward temperature")
    .withUnits("°C", SeriesDisplayMode.READOUT);

  public static final Quantity RETURN_TEMPERATURE = new Quantity("Return temperature")
    .withUnits("°C", SeriesDisplayMode.READOUT);

  public static final Quantity DIFFERENCE_TEMPERATURE = new Quantity("Difference temperature")
    .withUnits("K", SeriesDisplayMode.READOUT);

  public static final Quantity ENERGY_RETURN = new Quantity("Energy return")
    .withUnits("kWh", SeriesDisplayMode.CONSUMPTION);

  public static final Quantity REACTIVE_ENERGY = new Quantity("Reactive energy")
    .withUnits("kWh", SeriesDisplayMode.CONSUMPTION);

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
    REACTIVE_ENERGY,
    EXTERNAL_TEMPERATURE
  ));

  private static final String QUANTITY_UNIT_DELIMITER = ":";

  public final String name;

  @Nullable
  public final Integer id;
  @Nullable
  public final String storageUnit;
  private final QuantityPresentationInformation presentationInformation;

  public Quantity(
    @Nullable Integer id,
    String name,
    QuantityPresentationInformation presentationInformation,
    @Nullable String storageUnit
  ) {
    this.id = id;
    this.name = name;
    this.presentationInformation = presentationInformation;
    this.storageUnit = storageUnit;
  }

  public Quantity(
    @Nullable Integer id,
    String name,
    QuantityPresentationInformation presentationInformation
  ) {
    this(id, name, presentationInformation, null);
  }

  public Quantity(String name, QuantityPresentationInformation presentationInformation) {
    this(null, name, presentationInformation, null);
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

  public Quantity complementedBy(
    QuantityPresentationInformation presentationInformation,
    String storageUnit
  ) {
    String displayUnit = this.presentationInformation.getUnit()
      .orElse(presentationInformation.getUnit().orElseThrow(IllegalArgumentException::new));

    SeriesDisplayMode displayMode = seriesDisplayMode() == SeriesDisplayMode.UNKNOWN
      ? presentationInformation.displayMode
      : seriesDisplayMode();

    String defaultedStorageUnit = this.storageUnit == null ? storageUnit : this.storageUnit;

    return new Quantity(
      id,
      name,
      new QuantityPresentationInformation(displayUnit, displayMode),
      defaultedStorageUnit
    );
  }

  @Nullable
  @Override
  public Integer getId() {
    return id;
  }

  private Quantity withUnits(String unit, SeriesDisplayMode displayMode) {
    return new Quantity(
      id,
      name,
      new QuantityPresentationInformation(unit, displayMode),
      unit
    );
  }
}
