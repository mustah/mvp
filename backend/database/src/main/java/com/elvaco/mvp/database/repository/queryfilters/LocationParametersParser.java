package com.elvaco.mvp.database.repository.queryfilters;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.stream.Collectors.toList;

final class LocationParametersParser {

  private static final String DELIMITER = ",";

  private LocationParametersParser() {}

  static Parameters toCityParameters(List<String> cityIds) {
    Parameters parameters = new Parameters();
    toCityParams(cityIds)
      .forEach(cityParam -> {
        parameters.addCountry(cityParam.country);
        parameters.addCity(cityParam.city);
      });
    return parameters;
  }

  static Parameters toAddressParameters(List<String> addressIds) {
    Parameters parameters = new Parameters();
    toAddressParams(addressIds)
      .forEach(addressParam -> {
        parameters.addCountry(addressParam.country);
        parameters.addCity(addressParam.city);
        parameters.addAddress(addressParam.address);
      });
    return parameters;
  }

  static List<CityParam> toCityParams(List<String> cityIds) {
    return toParams(cityIds, LocationParametersParser::toCityParam);
  }

  static List<AddressParam> toAddressParams(List<String> addressIds) {
    return toParams(addressIds, LocationParametersParser::toAddressParam);
  }

  private static <T> List<T> toParams(List<String> ids, Function<String, T> toParamMapper) {
    return ids.stream()
      .map(toParamMapper)
      .filter(Objects::nonNull)
      .collect(toList());
  }

  @Nullable
  private static CityParam toCityParam(String cityId) {
    String[] args = trimAndSplit(cityId);
    return args.length == 2
      ? new CityParam(trimToLowerCase(args[0]), trimToLowerCase(args[1]))
      : null;
  }

  @Nullable
  private static AddressParam toAddressParam(String addressId) {
    String[] args = trimAndSplit(addressId);
    return args.length == 3
      ? new AddressParam(
      trimToLowerCase(args[0]),
      trimToLowerCase(args[1]),
      trimToLowerCase(args[2])
    )
      : null;
  }

  private static String trimToLowerCase(String s) {
    return s.trim().toLowerCase();
  }

  private static String[] trimAndSplit(String ids) {
    return ids.trim().split(DELIMITER);
  }

  @ToString
  @EqualsAndHashCode
  static class Parameters {

    final Set<String> countries;
    final Set<String> cities;
    final Set<String> addresses;

    private Parameters() {
      this.countries = new HashSet<>();
      this.cities = new HashSet<>();
      this.addresses = new HashSet<>();
    }

    boolean hasCities() {
      return !cities.isEmpty() && !countries.isEmpty();
    }

    public boolean hasAddresses() {
      return hasCities() && !addresses.isEmpty();
    }

    private void addCountry(String country) {
      countries.add(country);
    }

    private void addCity(String city) {
      cities.add(city);
    }

    private void addAddress(String address) {
      addresses.add(address);
    }
  }

  @ToString
  @EqualsAndHashCode
  static class CityParam {

    final String country;
    final String city;

    CityParam(String country, String city) {
      this.country = country;
      this.city = city;
    }
  }

  @EqualsAndHashCode
  @ToString
  static class AddressParam {

    final String country;
    final String city;
    final String address;

    AddressParam(String country, String city, String address) {
      this.country = country;
      this.city = city;
      this.address = address;
    }
  }
}
