package com.elvaco.mvp.core.domainmodels;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.core.domainmodels.DisplayMode.CONSUMPTION;
import static com.elvaco.mvp.core.domainmodels.DisplayMode.READOUT;
import static com.elvaco.mvp.core.domainmodels.Units.CUBIC_METRES;
import static com.elvaco.mvp.core.domainmodels.Units.CUBIC_METRES_PER_HOUR;
import static com.elvaco.mvp.core.domainmodels.Units.DEGREES_CELSIUS;
import static com.elvaco.mvp.core.domainmodels.Units.KELVIN;
import static com.elvaco.mvp.core.domainmodels.Units.KILOWATT_HOURS;
import static com.elvaco.mvp.core.domainmodels.Units.PERCENT;
import static com.elvaco.mvp.core.domainmodels.Units.WATT;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class DisplayQuantity {

  static final Set<DisplayQuantity> DEFAULT_GAS_DISPLAY_QUANTITIES = Set.of(
    new DisplayQuantity(
      Quantity.VOLUME,
      DisplayMode.CONSUMPTION,
      CUBIC_METRES
    )
  );
  static final Set<DisplayQuantity> DEFAULT_DISTRICT_DISPLAY_QUANTITIES = Set.of(
    new DisplayQuantity(
      Quantity.ENERGY,
      DisplayMode.CONSUMPTION,
      KILOWATT_HOURS
    ),
    new DisplayQuantity(
      Quantity.VOLUME,
      DisplayMode.CONSUMPTION,
      CUBIC_METRES
    ),
    new DisplayQuantity(
      Quantity.POWER,
      READOUT,
      WATT
    ),
    new DisplayQuantity(
      Quantity.VOLUME_FLOW,
      READOUT,
      CUBIC_METRES_PER_HOUR
    ),
    new DisplayQuantity(
      Quantity.FORWARD_TEMPERATURE,
      READOUT,
      DEGREES_CELSIUS
    ),
    new DisplayQuantity(
      Quantity.RETURN_TEMPERATURE,
      READOUT,
      DEGREES_CELSIUS
    ),
    new DisplayQuantity(
      Quantity.DIFFERENCE_TEMPERATURE,
      READOUT,
      KELVIN
    )
  );
  static final Set<DisplayQuantity> DEFAULT_ELECTRICITY_DISPLAY_QUANTITIES =
    Set.of(
      new DisplayQuantity(
        Quantity.ENERGY,
        CONSUMPTION,
        KILOWATT_HOURS
      ),

      new DisplayQuantity(
        Quantity.ENERGY_RETURN,
        CONSUMPTION,
        KILOWATT_HOURS
      ),
      new DisplayQuantity(
        Quantity.REACTIVE_ENERGY,
        CONSUMPTION,
        KILOWATT_HOURS
      ),
      new DisplayQuantity(
        Quantity.POWER,
        READOUT,
        WATT
      )
    );
  static final Set<DisplayQuantity> DEFAULT_ROOM_SENSOR_DISPLAY_QUANTITIES =
    Set.of(
      new DisplayQuantity(
        Quantity.EXTERNAL_TEMPERATURE,
        READOUT,
        DEGREES_CELSIUS
      ),
      new DisplayQuantity(
        Quantity.HUMIDITY,
        READOUT,
        PERCENT
      )
    );
  static final Set<DisplayQuantity> DEFAULT_WATER_DISPLAY_QUANTITIES = Set.of(
    new DisplayQuantity(
      Quantity.VOLUME,
      DisplayMode.CONSUMPTION,
      CUBIC_METRES
    )
  );
  private static final int DEFAULT_DECIMALS = 3;
  public final Quantity quantity;
  public final DisplayMode displayMode;
  public final int decimals;
  public final String unit;

  public DisplayQuantity(Quantity quantity, DisplayMode displayMode, String unit) {
    this(quantity, displayMode, DEFAULT_DECIMALS, unit);
  }

  public boolean isConsumption() {
    return displayMode == DisplayMode.CONSUMPTION;
  }
}
