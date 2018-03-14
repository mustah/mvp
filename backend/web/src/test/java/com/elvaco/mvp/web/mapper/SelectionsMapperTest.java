package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.web.dto.LocationsDto;
import com.elvaco.mvp.web.dto.LocationsDto.Address;
import com.elvaco.mvp.web.dto.LocationsDto.City;
import com.elvaco.mvp.web.dto.LocationsDto.Country;
import com.elvaco.mvp.web.dto.SelectionsDto;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SelectionsMapperTest {

  private SelectionsMapper mapper;
  private SelectionsDto selections;

  @Before
  public void setUp() {
    mapper = new SelectionsMapper();
    selections = new SelectionsDto();
  }

  @Test
  public void hasLocationTree() {
    Stream.of(
      new LocationBuilder()
        .country("sweden")
        .city("stockholm")
        .streetAddress("kungsgatan 1")
        .build(),
      new LocationBuilder()
        .country("sweden")
        .city("stockholm")
        .streetAddress("kungsgatan 2")
        .build(),
      new LocationBuilder()
        .country("unknown")
        .city("new york")
        .streetAddress("wall street")
        .build(),
      new LocationBuilder()
        .country("finland")
        .city("vasa")
        .streetAddress("street 1")
        .build()
    ).forEach(location -> mapper.addToDto(location, selections));

    LocationsDto locations = selections.locations;

    assertThat(locations.getCountries()).hasSize(3);

    assertThat(locations.getCountry("sweden").getCities()).containsExactly(new City("stockholm"));
    assertThat(locations.getCountry("sweden").getCity("stockholm").getAddresses())
      .containsExactly(
        new Address("kungsgatan 1"),
        new Address("kungsgatan 2")
      );

    assertThat(locations.getCountry("unknown").getCities()).containsExactly(new City("new york"));
    assertThat(locations.getCountry("unknown").getCity("new york").getAddresses())
      .containsExactly(new Address("wall street"));

    assertThat(locations.getCountry("finland").getCities()).containsExactly(new City("vasa"));
    assertThat(locations.getCountry("finland").getCity("vasa").getAddresses())
      .containsExactly(new Address("street 1"));
  }

  @Test
  public void locationsShouldNotHaveAnyCountriesWhenThereIsNoAddressSpecified() {
    Location location = new LocationBuilder()
      .country("unknown")
      .city("new york")
      .build();

    List<Country> countries = mapper.addToDto(location, selections)
      .locations
      .getCountries();

    assertThat(countries).isEmpty();
  }
}