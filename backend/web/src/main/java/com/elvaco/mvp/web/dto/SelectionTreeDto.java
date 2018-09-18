package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
public class SelectionTreeDto {

  public List<CityDto> cities;

  public SelectionTreeDto(List<CityDto> cities) {
    this.cities = cities;
  }

  @NoArgsConstructor
  @EqualsAndHashCode(exclude = "addresses")
  @ToString(exclude = "addresses")
  public static class CityDto {
    public Set<String> medium;
    public String id;
    public String name;
    public List<AddressDto> addresses;

    public CityDto(String id, String name, Set<String> medium, List<AddressDto> addresses) {
      this.id = id;
      this.name = name;
      this.medium = medium;
      this.addresses = addresses;
    }

    public CityDto(String id, String name, Set<String> medium) {
      this.id = id;
      this.name = name;
      this.medium = medium;
    }
  }

  @NoArgsConstructor
  @EqualsAndHashCode(exclude = "meters")
  @ToString(exclude = "meters")
  public static class AddressDto {
    public String name;
    public List<MeterDto> meters;

    public AddressDto(String name, List<MeterDto> meters) {
      this.name = name;
      this.meters = meters;
    }

    public AddressDto(String name) {
      this.name = name;
    }
  }

  @NoArgsConstructor
  @EqualsAndHashCode
  @ToString
  public static class MeterDto {
    public UUID id;
    public String name;
    public String medium;

    public MeterDto(UUID id, String name, String medium) {
      this.id = id;
      this.name = name;
      this.medium = medium;
    }
  }
}
