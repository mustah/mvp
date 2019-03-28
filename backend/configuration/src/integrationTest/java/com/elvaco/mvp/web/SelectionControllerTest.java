package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.OrganisationWithUsers;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.CityDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.spi.data.RequestParameter.Q;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.WILDCARD;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionControllerTest extends IntegrationTest {

  @Autowired
  private UserSelections userSelections;

  @After
  public void tearDown() {
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void getCities() {
    prepareMeters();

    Page<CityDto> response = asUser()
      .getPage("/selections/cities", CityDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactlyInAnyOrder(
      new CityDto("kungsbacka", "sweden"),
      new CityDto("gothenburg", "sweden"),
      new CityDto("helsinki", "finland")
    );
  }

  @Test
  public void getCities_SortedByCityAsc() {
    prepareMeters();

    Page<CityDto> response = asUser()
      .getPage("/selections/cities?sort=city", CityDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new CityDto("gothenburg", "sweden"),
      new CityDto("helsinki", "finland"),
      new CityDto("kungsbacka", "sweden")
    );
  }

  @Test
  public void getCities_SortedByCityDesc() {
    prepareMeters();

    Page<CityDto> response = asUser()
      .getPage("/selections/cities?sort=city,desc", CityDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new CityDto("kungsbacka", "sweden"),
      new CityDto("helsinki", "finland"),
      new CityDto("gothenburg", "sweden")
    );
  }

  @Test
  public void getAddresses_SortAsc() {
    prepareMeters();

    Page<AddressDto> response = asUser()
      .getPage("/selections/addresses?sort=streetAddress,asc", AddressDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new AddressDto("finland", "helsinki", "joksigatan 2", null),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 1", "43437"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 2", "43437"),
      new AddressDto("sweden", "gothenburg", "snabelgatan 3", null)
    );
  }

  @Test
  public void getAddresses_SortDesc() {
    prepareMeters();

    Page<AddressDto> response = asUser()
      .getPage("/selections/addresses?sort=streetAddress,desc", AddressDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new AddressDto("sweden", "gothenburg", "snabelgatan 3", null),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 2", "43437"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 1", "43437"),
      new AddressDto("finland", "helsinki", "joksigatan 2", null)
    );
  }

  @Test
  public void getAddresses_IgnoresNull() {
    prepareMeters();
    given(logicalMeter().externalId("extId17")
      .location(kungsbacka().country(null).address("kabelgatan 17").build())
    );

    Page<AddressDto> response = asUser()
      .getPage("/selections/addresses?sort=streetAddress,asc", AddressDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new AddressDto("finland", "helsinki", "joksigatan 2", null),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 1", "43437"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 2", "43437"),
      new AddressDto("sweden", "gothenburg", "snabelgatan 3", null)
    );
  }

  @Test
  public void userCanNotAccessOtherOrganisationsCities() {
    OrganisationWithUsers organisationWithUsers = given(organisation(), user());
    given(
      logicalMeter()
        .location(kungsbacka().country("sweden").address("kabelgatan 1").build())
        .externalId("extId1"),
      logicalMeter()
        .location(kungsbacka().country("sweden").address("kabelgatan 2").build())
        .externalId("extId2"),
      logicalMeter()
        .location(kungsbacka().country("sweden").address("kabelgatan 3").build())
        .externalId("extId3"),
      logicalMeter()
        .location(stockholm().country("sweden").address("kungsgatan 3").build())
        .organisationId(organisationWithUsers.getId())
        .externalId("extId4")
    );

    Page<CityDto> response = as(organisationWithUsers.getUser()).getPage(
      "/selections/cities",
      CityDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new CityDto("stockholm", "sweden"));
  }

  @Test
  public void getFacilities_SortedAsc() {
    prepareMeters();

    var url = facilitiesUrl()
      .sortBy("externalId,asc")
      .build();

    Page<IdNamedDto> response = asUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("extId1"),
      new IdNamedDto("extId2"),
      new IdNamedDto("extId3"),
      new IdNamedDto("extId4")
    );
  }

  @Test
  public void getFacilities_SortedDesc() {
    prepareMeters();

    var url = facilitiesUrl()
      .sortBy("externalId,desc")
      .build();

    Page<IdNamedDto> response = asUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("extId4"),
      new IdNamedDto("extId3"),
      new IdNamedDto("extId2"),
      new IdNamedDto("extId1")
    );
  }

  @Test
  public void getFacilities_FilteredOnFacilitySearchText() {
    prepareMeters();

    var url = facilitiesUrl()
      .parameter(Q, "3")
      .sortBy("externalId,asc")
      .build();

    Page<IdNamedDto> response = asUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("extId3"));
  }

  @Test
  public void userCanNotAccessOtherOrganisationsFacilities() {
    prepareMeters();
    OrganisationWithUsers organisationWithUsers = given(organisation(), user());
    given(
      logicalMeter().location(kungsbacka().country("sweden").address("kabelgatan 2").build())
        .externalId("extId5"),

      logicalMeter().location(kungsbacka().city("gothenburg")
        .country("sweden")
        .address("snabelgatan 3")
        .build())
        .externalId("extId6")
        .organisationId(organisationWithUsers.getId())

    );

    var url = facilitiesUrl()
      .sortBy("externalId,asc")
      .parameter(Q, "extId6")
      .build();

    Page<IdNamedDto> response = asUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
    assertThat(response.getContent()).isEmpty();

    response = as(organisationWithUsers.getUser()).getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("extId6"));
  }

  @Test
  public void getSecondaryAddresses_SortedDesc() {
    prepareMeters();

    Page<IdNamedDto> response = asUser()
      .getPage("/selections/secondary-addresses?sort=secondaryAddress,desc", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("444"),
      new IdNamedDto("333"),
      new IdNamedDto("222"),
      new IdNamedDto("111")
    );
  }

  @Test
  public void getSecondaryAddressFiltered() {
    prepareMeters();

    Page<IdNamedDto> response = asUser()
      .getPage("/selections/secondary-addresses?q=444", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("444"));
  }

  @Test
  public void userCanNotAccessOtherOrganisationsSecondaryAddresses() {
    prepareMeters();

    OrganisationWithUsers organisationWithUsers = given(organisation(), user());
    given(
      logicalMeter()
        .organisationId(organisationWithUsers.getId())
        .location(kungsbacka().country("finland").city("helsinki").address("joksigatan 2").build())
        .externalId("extId777"),
      physicalMeter().address("777").organisationId(organisationWithUsers.getId())
    );

    String url = "/selections/secondary-addresses?q=777";

    Page<IdNamedDto> response = asUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
    assertThat(response.getContent()).isEmpty();

    response = as(organisationWithUsers.getUser()).getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("777"));
  }

  @Test
  public void getGatewaySerials_SortedDesc() {
    prepareGateways();

    Page<IdNamedDto> response = asUser()
      .getPage(gatewaySerialsUrl().sortBy("serial,desc").build(), IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("5566"),
      new IdNamedDto("3344"),
      new IdNamedDto("1122")
    );
  }

  @Test
  public void getOrganisations_SortedAsc() {
    given(
      organisation().name("testing-bcda"),
      organisation().name("testing-abcd"),
      organisation().name("testing-dabc"),
      organisation().name("testing-cdab")
    );

    Page<OrganisationDto> response = asSuperAdmin()
      .getPage(
        organisationsUrl()
          .filter("testing") // Exclude "helpful" default organisation & context organisation
          .sortBy("name,asc").build(),
        OrganisationDto.class
      );

    assertThat(response.getContent())
      .extracting(organisationDto -> organisationDto.name)
      .containsExactly("testing-abcd", "testing-bcda", "testing-cdab", "testing-dabc");
  }

  @Test
  public void getOrganisations_SortedDesc() {
    given(
      organisation().name("testing-bcda"),
      organisation().name("testing-abcd"),
      organisation().name("testing-dabc"),
      organisation().name("testing-cdab")
    );

    Page<OrganisationDto> response = asSuperAdmin()
      .getPage(
        organisationsUrl()
          .filter("testing") // Exclude "helpful" default organisation & context organisation
          .sortBy("name,desc").build(),
        OrganisationDto.class
      );

    assertThat(response.getContent())
      .extracting(organisationDto -> organisationDto.name)
      .containsExactly("testing-dabc", "testing-cdab", "testing-bcda", "testing-abcd");
  }

  @Test
  public void getGatewaySerials_FilteredOnQueryString() {
    prepareGateways();

    Url url = gatewaySerialsUrl()
      .parameter(SERIAL, "66")
      .build();

    Page<IdNamedDto> response = asUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).extracting("id").containsExactly("5566");
  }

  @Test
  public void userCanNotAccessOtherOrganisationsGatewaySerials() {
    prepareGateways();
    OrganisationWithUsers organisationWithUsers = given(organisation(), user());
    given(gateway().organisationId(organisationWithUsers.getId())
      .serial("6666")
      .productModel("3100"));

    var response = as(organisationWithUsers.getUser()).getPage(
      gatewaySerialsUrl().build(),
      IdNamedDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).extracting("id").containsExactly("6666");
  }

  @Test
  public void wildcard_facility() {
    given(logicalMeter().externalId("abcdef"));

    var contains = asUser().getPage(
      Url.builder().path("/selections/facilities").parameter(Q, "bcd").build(),
      IdNamedDto.class
    );
    assertThat(contains).extracting("name").containsExactly("abcdef");

    var startsWith = asUser().getPage(
      Url.builder().path("/selections/facilities").parameter(Q, "abcd").build(),
      IdNamedDto.class
    );
    assertThat(startsWith).extracting("name").containsExactly("abcdef");

    var emptySearchResponse = asUser().getPage(
      Url.builder().path("/selections/facilities").parameter(Q, "qwerty").build(),
      IdNamedDto.class
    );
    assertThat(emptySearchResponse).isEmpty();
  }

  @Test
  public void wildcard_secondaryAddress() {
    given(
      logicalMeter().externalId("1234"), physicalMeter().address("123456")
    );

    var response = asUser().getPage(Url.builder()
      .path("/selections/secondary-addresses")
      .parameter(Q, "12345")
      .build(), IdNamedDto.class);

    assertThat(response).hasSize(1);
    assertThat(response.getContent().get(0).name).isEqualTo("123456");

    var emptySearchResponse = asUser().getPage(Url.builder()
      .path("/selections/secondary-addresses")
      .parameter(Q, "000000")
      .build(), IdNamedDto.class);

    assertThat(emptySearchResponse).isEmpty();
  }

  @Test
  public void wildcard_gatewaySerial() {
    given(gateway().serial("1234567"));

    var contains = asUser().getPage(Url.builder().path("/selections/gateway-serials")
      .parameter(Q, "3456").build(), IdNamedDto.class);
    assertThat(contains.getContent()).extracting(gw -> gw.name).containsExactly("1234567");

    var startsWith = asUser().getPage(Url.builder().path("/selections/gateway-serials")
      .parameter(Q, "1234").build(), IdNamedDto.class);
    assertThat(startsWith.getContent()).extracting(gw -> gw.name).containsExactly("1234567");

    assertThat(asUser().getPage(
      Url.builder().path("/selections/gateway-serials")
        .parameter(Q, "90909090")
        .build(),
      IdNamedDto.class
    )).isEmpty();
  }

  @Test
  public void wildcard_city() {
    given(logicalMeter().location(kungsbacka().build()));

    Page<CityDto> contains = asUser().getPage("/selections/cities?q=ngsback", CityDto.class);
    assertThat(contains.getContent()).extracting(c -> c.name).containsExactly("kungsbacka");

    Page<CityDto> startsWith = asUser().getPage("/selections/cities?q=kungs", CityDto.class);
    assertThat(startsWith.getContent()).extracting(c -> c.name).containsExactly("kungsbacka");

    assertThat(asUser().getPage("/selections/cities?q=tockholm", IdNamedDto.class)).hasSize(0);
  }

  @Test
  public void wildcard_streetAddress() {
    given(logicalMeter().location(kungsbacka().address("Stora vägen 24").build()));

    Page<AddressDto> contains = asUser().getPage("/selections/addresses?q=tora", AddressDto.class);
    assertThat(contains.getContent()).extracting(a -> a.street).containsExactly("stora vägen 24");

    Page<AddressDto> startsWith = asUser().getPage(
      "/selections/addresses?q=stora",
      AddressDto.class
    );
    assertThat(startsWith.getContent()).extracting(a -> a.street).containsExactly("stora vägen 24");

    assertThat(asUser().getPage(
      "/selections/addresses?q=illa",
      AddressDto.class
    )).hasSize(0);
  }

  @Test
  public void wildcard_organisation() {
    given(
      organisation().name("abcdef"),
      organisation().name("defghi")
    );

    assertThat(asSuperAdmin()
      .getPage(
        organisationsUrl()
          .parameter(WILDCARD, "abc")
          .build(),
        OrganisationDto.class
      )
    )
      .as("Case insensitive substring in the start of term")
      .extracting(o -> o.name)
      .containsExactly(
        "abcdef"
      );

    assertThat(asSuperAdmin()
      .getPage(
        organisationsUrl()
          .parameter(WILDCARD, "de")
          .build(),
        OrganisationDto.class
      )
    )
      .as("Substring in the middle of term")
      .extracting(o -> o.name)
      .containsExactly(
        "abcdef",
        "defghi"
      );

    assertThat(asSuperAdmin()
      .getPage(
        organisationsUrl()
          .parameter(WILDCARD, "asdfasdf12341234osadkfj2435dsfkjdsfkjtnh42")
          .build(),
        OrganisationDto.class
      )
    ).as("Wild card does not mean 'select everything'")
      .hasSize(0);
  }

  @Test
  public void getCities_duplicatesAreNotIncluded() {
    given(
      logicalMeter().location(kungsbacka().build()),
      logicalMeter().location(kungsbacka().build()),
      logicalMeter().location(stockholm().country("norge").city("oslo").build())
    );

    Page<CityDto> response = asUser().getPage(
      "/selections/cities",
      CityDto.class
    );

    assertThat(response.getContent()).containsExactlyInAnyOrder(
      new CityDto("kungsbacka", "sverige"),
      new CityDto("oslo", "norge")
    );
  }

  @Test
  public void getAddresses_duplicatesAreNotIncluded() {
    given(
      logicalMeter().location(kungsbacka().build()),
      logicalMeter().location(kungsbacka().build())
    );

    Page<AddressDto> response = asUser().getPage(
      "/selections/addresses",
      AddressDto.class
    );

    assertThat(response.getContent()).containsExactly(
      new AddressDto("sverige", "kungsbacka", "kabelgatan 1", null)
    );
  }

  @Test
  public void getSecondaryAddresses_duplicatesAreNotIncluded() {
    given(logicalMeter(), physicalMeter().address("123456"));
    given(logicalMeter(), physicalMeter().address("123456"));

    Page<IdNamedDto> response = asUser().getPage(
      "/selections/secondary-addresses",
      IdNamedDto.class
    );

    assertThat(response).extracting("name").containsExactly("123456");
  }

  @Test
  public void getFacilities_duplicatesAreNotIncluded() {
    given(
      logicalMeter().externalId("1234"),
      physicalMeter().externalId("1234"),
      physicalMeter().externalId("1234")
    );

    Page<IdNamedDto> response = asUser().getPage(
      "/selections/facilities",
      IdNamedDto.class
    );

    assertThat(response).extracting("name").containsExactly("1234");
  }

  @Test
  public void getGatewaySerials_duplicatesAreNotIncluded() {
    given(
      gateway().serial("1").productModel("3100"),
      gateway().serial("1").productModel("2100"),
      gateway().serial("2").productModel("3100")
    );

    Page<IdNamedDto> response = asUser().getPage(gatewaySerialsUrl().build(), IdNamedDto.class);

    assertThat(response).extracting("name").containsExactlyInAnyOrder("1", "2");
  }

  @Test
  public void getOrganisations_allowedBySuperAdmin() {
    assertThat(organisations.findAll())
      .as("The test fixtures added some organisations for us")
      .isNotEmpty();

    Page<IdNamedDto> response = asSuperAdmin()
      .getPage("/selections/organisations", IdNamedDto.class);

    assertThat(response).isNotEmpty();
  }

  @Test
  public void getOrganisations_disallowedByUser() {
    assertThat(organisations.findAll())
      .as("The test fixtures added some organisations for us")
      .isNotEmpty();

    Page<IdNamedDto> response = asUser()
      .getPage("/selections/organisations", IdNamedDto.class);

    assertThat(response).isEmpty();
  }

  @Test
  public void getOrganisations_excludesSubOrganisations() {
    var userSelection = userSelections.save(userSelection().build());
    var subOrganisation = createSubOrganisation(userSelection);

    Page<IdNamedDto> response = asSuperAdmin()
      .getPage("/selections/organisations", IdNamedDto.class);

    assertThat(response.getContent())
      .extracting("id")
      .contains(subOrganisation.parent.id.toString())
      .doesNotContain(subOrganisation.id.toString());
  }

  private Organisation createSubOrganisation(UserSelection userSelection) {
    return organisations.save(Organisation.builder()
      .name("sub-org")
      .slug("sub-org")
      .externalId("sub-org")
      .parent(context().defaultOrganisation())
      .selection(userSelection)
      .build()
    );
  }

  private UserSelection.UserSelectionBuilder userSelection() {
    return UserSelection.builder()
      .id(randomUUID())
      .name("a-user-selection")
      .ownerUserId(context().superAdmin.id)
      .organisationId(context().organisationId())
      .selectionParameters(toJsonNode(CITIES_JSON_STRING));
  }

  private void prepareGateways() {
    given(
      gateway().productModel("3100").serial("1122"),
      gateway().productModel("3100").serial("3344"),
      gateway().productModel("3100").serial("5566")
    );
  }

  private void prepareMeters() {
    given(
      logicalMeter()
        .externalId("extId1")
        .location(kungsbacka().address("kabelgatan 1")
          .city("kungsbacka")
          .country("sweden")
          .zip("43437")
          .build()),
      physicalMeter().address("111")
    );

    given(
      logicalMeter()
        .externalId("extId2")
        .location(kungsbacka().address("kabelgatan 2")
          .city("kungsbacka")
          .country("sweden")
          .zip("43437")
          .build()),
      physicalMeter().address("222")
    );

    given(
      logicalMeter()
        .externalId("extId3")
        .location(kungsbacka().address("snabelgatan 3")
          .city("gothenburg")
          .country("sweden")
          .zip("")
          .build()),
      physicalMeter().address("333")
    );

    given(
      logicalMeter()
        .externalId("extId4")
        .location(kungsbacka().address("joksigatan 2")
          .city("helsinki")
          .country("finland")
          .zip("")
          .build()),
      physicalMeter().address("444")
    );
  }

  private static Url.UrlBuilder organisationsUrl() {
    return Url.builder().path("/selections/organisations");
  }

  private static Url.UrlBuilder gatewaySerialsUrl() {
    return Url.builder().path("/selections/gateway-serials");
  }

  private static Url.UrlBuilder facilitiesUrl() {
    return Url.builder().path("/selections/facilities");
  }
}
