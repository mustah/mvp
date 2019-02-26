package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder(toBuilder = true)
public class Quantity implements Identifiable<Integer> {

  public static final Quantity EXTERNAL_TEMPERATURE = new QuantityBuilder()
    .name("External temperature").storageUnit("°C").build();

  public static final Quantity VOLUME = new QuantityBuilder()
    .name("Volume").storageUnit("m³").build();

  public static final Quantity VOLUME_FLOW = new QuantityBuilder()
    .name("Flow").storageUnit("m³/h").build();

  public static final Quantity TEMPERATURE = new QuantityBuilder()
    .name("Temperature").storageUnit("°C").build();

  public static final Quantity HUMIDITY = new QuantityBuilder()
    .name("Relative humidity").storageUnit("%").build();

  public static final Quantity ENERGY = new QuantityBuilder()
    .name("Energy").storageUnit("kWh").build();

  public static final Quantity POWER = new QuantityBuilder()
    .name("Power").storageUnit("W").build();

  public static final Quantity FORWARD_TEMPERATURE = new QuantityBuilder()
    .name("Forward temperature").storageUnit("°C").build();

  public static final Quantity RETURN_TEMPERATURE = new QuantityBuilder()
    .name("Return temperature").storageUnit("°C").build();

  public static final Quantity DIFFERENCE_TEMPERATURE = new QuantityBuilder()
    .name("Difference temperature").storageUnit("K").build();

  public static final Quantity ENERGY_RETURN = new QuantityBuilder()
    .name("Energy return").storageUnit("kWh").build();

  public static final Quantity REACTIVE_ENERGY = new QuantityBuilder()
    .name("Reactive energy").storageUnit("kWh").build();

  public static final List<Quantity> QUANTITIES = List.of(
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
  );

  @Nullable
  public final Integer id;
  public final String name;
  public final String storageUnit;

  public boolean isConsumptionByDefault() {
    //TODO: This is a hack to support measurement thresholds. We should decide how to get the
    // display information to the threshold parser
    return Set.of(VOLUME.name, ENERGY.name, REACTIVE_ENERGY.name, ENERGY_RETURN.name)
      .contains(this.name);
  }

  @Nullable
  @Override
  public Integer getId() {
    return id;
  }
}
