package com.elvaco.mvp.database.repository.queryfilters;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN;
import static com.elvaco.mvp.core.util.CollectionHelper.isNotEmpty;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class LocationParametersParser {

  private static final String DELIMITER = ",";

  public static Parameters toCityParameters(Collection<String> cityIds) {
    var parameters = new Parameters();
    toCityParams(cityIds).forEach(cityParam -> {
      parameters.addCountry(cityParam.country);
      parameters.addCity(cityParam.city);
    });
    return parameters;
  }

  public static Parameters toAddressParameters(Collection<String> addressIds) {
    var parameters = new Parameters();
    toAddressParams(addressIds).forEach(addressParam -> {
      parameters.addCountry(addressParam.country);
      parameters.addCity(addressParam.city);
      parameters.addAddress(addressParam.address);
    });
    return parameters;
  }

  static List<CityParam> toCityParams(Collection<String> cityIds) {
    return toParams(cityIds, LocationParametersParser::toCityParam);
  }

  static List<AddressParam> toAddressParams(Collection<String> addressIds) {
    return toParams(addressIds, LocationParametersParser::toAddressParam);
  }

  private static <T> List<T> toParams(Collection<String> ids, Function<String, T> toParamMapper) {
    return ids.stream()
      .map(toParamMapper)
      .filter(Objects::nonNull)
      .toList();
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

    boolean hasUnknownCountries;
    boolean hasUnknownCities;
    boolean hasUnknownAddresses;

    private Parameters() {
      this.countries = new HashSet<>();
      this.cities = new HashSet<>();
      this.addresses = new HashSet<>();
    }

    boolean hasCountriesAndCities() {
      return isNotEmpty(cities) && isNotEmpty(countries);
    }

    boolean hasAddresses() {
      return hasCountriesAndCities() && isNotEmpty(addresses);
    }

    private void addCountry(String country) {
      if (UNKNOWN.equalsIgnoreCase(country)) {
        hasUnknownCountries = true;
      } else {
        countries.add(country);
      }
    }

    private void addCity(String city) {
      if (UNKNOWN.equalsIgnoreCase(city)) {
        hasUnknownCities = true;
      } else {
        cities.add(city);
      }
    }

    private void addAddress(String address) {
      if (UNKNOWN.equalsIgnoreCase(address)) {
        hasUnknownAddresses = true;
      } else {
        addresses.add(address);
      }
    }
  }

  @AllArgsConstructor
  @ToString
  @EqualsAndHashCode
  static class CityParam {

    final String country;
    final String city;
  }

  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  static class AddressParam {

    final String country;
    final String city;
    final String address;
  }
}
