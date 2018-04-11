package com.elvaco.mvp.core.domainmodels;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MeterSummary {

  private final Map<UUID, LogicalMeter> meters = new HashMap<>();
  private final Map<String, Location> cities = new HashMap<>();
  private final Map<String, Location> addresses = new HashMap<>();

  public MeterSummary add(LogicalMeter logicalMeter) {
    Location location = logicalMeter.location;
    meters.put(logicalMeter.id, logicalMeter);

    if (location.isKnown()) {
      cities.put(makeCityKeyOf(location), location);
      addresses.put(makeAddressKeyOf(location), location);
    }

    return this;
  }

  public int numMeters() {
    return meters.size();
  }

  public int numCities() {
    return cities.size();
  }

  public int numAddresses() {
    return addresses.size();
  }

  private String makeAddressKeyOf(Location location) {
    return String.format(
      "%s:%s:%s",
      location.getCountry(),
      location.getCity(),
      location.getAddress()
    );
  }

  private String makeCityKeyOf(Location location) {
    return String.format("%s:%s", location.getCountry(), location.getCity());
  }
}
