package com.elvaco.mvp.core.domainmodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

public class SelectionTree {

  private final Map<String, City> cities = new HashMap<>();

  public void add(LogicalMeter logicalMeter) {
    Location location = logicalMeter.location;

    String country = location.getCountryOrUnknown();
    String city = location.getCityOrUnknown();
    String address = location.getAddressOrUnknown();

    String cityId = country.concat(",").concat(city);
    addCity(cityId, city)
      .addMedium(logicalMeter.getMedium())
      .addAddress(address)
      .addMeter(logicalMeter.id, logicalMeter.externalId, logicalMeter.getMedium());
  }

  public City getCity(String id) {
    return cities.get(id);
  }

  public List<City> getCities() {
    return new ArrayList<>(cities.values());
  }

  private City addCity(String id, String name) {
    return cities.computeIfAbsent(id, (key) -> new City(key, name));
  }

  @ToString(exclude = "addresses")
  @EqualsAndHashCode(exclude = "addresses")
  public static class City {
    public final String name;
    public final String id;
    public final Set<String> medium = new HashSet<>();
    private final Map<String, Address> addresses = new HashMap<>();

    private City(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public Address getAddress(String name) {
      return addresses.get(name);
    }

    public List<Address> getAddresses() {
      return new ArrayList<>(addresses.values());
    }

    public City addMedium(String medium) {
      this.medium.add(medium);
      return this;
    }

    private Address addAddress(String name) {
      return addresses.computeIfAbsent(name, Address::new);
    }
  }

  @ToString(exclude = "meters")
  @EqualsAndHashCode(exclude = "meters")
  public static class Address {
    public final String name;
    private final Map<UUID, Meter> meters = new HashMap<>();

    private Address(String name) {
      this.name = name;
    }

    public List<Meter> getMeters() {
      return new ArrayList<>(meters.values());
    }

    void addMeter(UUID id, String name, String medium) {
      meters.computeIfAbsent(id, (key) -> new Meter(key, name, medium));
    }
  }

  @ToString
  @EqualsAndHashCode
  public static class Meter {
    public final String name;
    public final UUID id;
    public final String medium;

    private Meter(UUID id, String name, String medium) {
      this.id = id;
      this.name = name;
      this.medium = medium;
    }
  }
}
