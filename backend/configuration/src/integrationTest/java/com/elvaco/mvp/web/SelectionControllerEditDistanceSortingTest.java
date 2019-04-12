package com.elvaco.mvp.web;

import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.CityDto;

import org.junit.Test;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionControllerEditDistanceSortingTest extends IntegrationTest {

  @Test
  public void cities_FilteredAndOrderedByEditDistanceByDefault() {
    given(
      logicalMeter().location(kungsbacka().build()),
      logicalMeter().location(kungsbacka().city("kungshamn").build()),
      logicalMeter().location(kungsbacka().city("kunn").build()),
      logicalMeter().location(kungsbacka().city("stockholm").build())
    );

    Url url = Url.builder()
      .path("/selections/cities")
      .filter("kun")
      .page(0)
      .build();

    Page<CityDto> response = asUser()
      .getPage(url, CityDto.class);

    assertThat(response.getContent().stream().map(cityDto -> cityDto.name)).containsExactly(
      "kunn", "kungshamn", "kungsbacka"
    );
  }

  @Test
  public void streetAddresses_FilteredAndOrderedByEditDistanceByDefault() {
    given(
      logicalMeter().location(kungsbacka().address("gatan").build()),
      logicalMeter().location(kungsbacka().address("vintergatan").build()),
      logicalMeter().location(kungsbacka().address("intergatan").build()),
      logicalMeter().location(kungsbacka().address("agatan").build()),
      logicalMeter().location(kungsbacka().address("vägen").build())
    );

    Url url = Url.builder()
      .path("/selections/addresses")
      .filter("gata")
      .page(0)
      .build();

    Page<AddressDto> response = asUser()
      .getPage(url, AddressDto.class);

    assertThat(response.getContent().stream().map(addressDto -> addressDto.street)).containsExactly(
      "gatan", "agatan", "intergatan", "vintergatan"
    );
  }

  @Test
  public void facilities_FilteredAndOrderedByEditDistanceByDefault() {
    given(
      logicalMeter().externalId("aaaa"),
      logicalMeter().externalId("abaa"),
      logicalMeter().externalId("caa"),
      logicalMeter().externalId("aa"),
      logicalMeter().externalId("bbbb")
    );

    Url url = Url.builder()
      .path("/selections/facilities")
      .filter("aa")
      .page(0)
      .build();

    Page<IdNamedDto> response = asUser()
      .getPage(url, IdNamedDto.class);

    assertThat(response.getContent().stream().map(facility -> facility.name)).containsExactly(
      "aa", "caa", "aaaa", "abaa"
    );
  }

  @Test
  public void secondaryAddresses_FilteredAndOrderedByEditDistanceByDefault() {
    given(
      logicalMeter().physicalMeter(physicalMeter().address("aaaa").build()),
      logicalMeter().physicalMeter(physicalMeter().address("abaa").build()),
      logicalMeter().physicalMeter(physicalMeter().address("caa").build()),
      logicalMeter().physicalMeter(physicalMeter().address("aa").build()),
      logicalMeter().physicalMeter(physicalMeter().address("bbbb").build())
    );

    Url url = Url.builder()
      .path("/selections/secondary-addresses")
      .filter("aa")
      .page(0)
      .build();

    Page<IdNamedDto> response = asUser()
      .getPage(url, IdNamedDto.class);

    assertThat(response.getContent().stream().map(addr -> addr.name)).containsExactly(
      "aa", "caa", "aaaa", "abaa"
    );
  }

  @Test
  public void gatewaySerials_FilteredAndOrderedByEditDistanceByDefault() {
    given(
      gateway().serial("aaaa"),
      gateway().serial("abaa"),
      gateway().serial("caa"),
      gateway().serial("aa"),
      gateway().serial("bbbb")
    );

    Url url = Url.builder()
      .path("/selections/gateway-serials")
      .filter("aa")
      .page(0)
      .build();

    Page<IdNamedDto> response = asUser()
      .getPage(url, IdNamedDto.class);

    assertThat(response.getContent().stream().map(serial -> serial.name)).containsExactly(
      "aa", "caa", "aaaa", "abaa"
    );
  }

  @Test
  public void organisations_FilteredAndOrderedByEditDistanceByDefault() {
    given(
      organisation().name("aaaa"),
      organisation().name("abaa"),
      organisation().name("caa"),
      organisation().name("aa"),
      organisation().name("bbbb")
    );

    Url url = Url.builder()
      .path("/selections/organisations")
      .filter("aa")
      .page(0)
      .build();

    Page<IdNamedDto> response = asSuperAdmin() //For super admin eyes only
      .getPage(url, IdNamedDto.class);

    var organisationNames = response.getContent().stream().map(organisation -> organisation.name);

    assertThat(organisationNames).containsExactly("aa", "caa", "aaaa", "abaa");
  }
}
