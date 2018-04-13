package com.elvaco.mvp.web.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
public class LocationsDto {

  private final Map<String, Country> countries = new HashMap<>();

  private Country addCountry(String country) {
    return countries.computeIfAbsent(country, Country::new);
  }

  public void addLocation(String country, String city, String address) {
    addCountry(country)
      .addCity(city)
      .addAddress(address);
  }

  public Country getCountry(String name) {
    return countries.get(name);
  }

  public List<Country> getCountries() {
    return new ArrayList<>(countries.values());
  }

  @ToString(exclude = "cities")
  @EqualsAndHashCode(exclude = "cities")
  public static class Country {
    public final String name;
    private final Map<String, City> cities = new HashMap<>();

    private Country(String name) {
      this.name = name;
    }

    private City addCity(String city) {
      return this.cities.computeIfAbsent(city, City::new);
    }

    public City getCity(String city) {
      return this.cities.get(city);
    }

    public List<City> getCities() {
      return new ArrayList<>(cities.values());
    }
  }

  @ToString(exclude = "addresses")
  @EqualsAndHashCode(exclude = "addresses")
  public static class City {
    public final String name;
    private final Map<String, Address> addresses = new HashMap<>();

    public City(String name) {
      this.name = name;
    }

    private Address addAddress(String address) {
      return this.addresses.computeIfAbsent(address, Address::new);
    }

    public List<Address> getAddresses() {
      return new ArrayList<>(addresses.values());
    }
  }

  @ToString
  @EqualsAndHashCode
  public static class Address {
    public final String name;

    public Address(String name) {
      this.name = name;
    }
  }
}

