package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.CityDto;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.oslo;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.FACILITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserTestData.subOrgUser;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionControllerSubOrganisationTest extends IntegrationTest {

  @Autowired
  private UserSelections userSelections;

  @Test
  public void excludeAddresses_NotIncluded_InSelectionParameters() {
    var userSelection = userSelections.save(subOrganisationUserSelection().build());

    var subOrganisation = createSubOrganisation(userSelection);

    given(
      logicalMeter().location(kungsbacka().build()),
      logicalMeter().location(stockholm().build()),
      logicalMeter().location(oslo().build())
    );

    var user = subOrgUser().organisation(subOrganisation).build();

    var url = Url.builder()
      .path("/selections/addresses")
      .sortBy("streetAddress,asc")
      .build();

    var response = as(user).getPage(url, AddressDto.class);

    assertThat(response.getContent()).containsExactly(
      new AddressDto("sverige", "stockholm", "drottninggatan 1337", null),
      new AddressDto("sverige", "kungsbacka", "kabelgatan 1",null)
    );
  }

  @Test
  public void noCities_WhenCityParameterNotIncluded_InSelectionParameters() {
    var userSelection = userSelections.save(subOrganisationUserSelection().build());

    var subOrganisation = createSubOrganisation(userSelection);

    given(logicalMeter().location(kungsbacka().build()));

    var user = subOrgUser().organisation(subOrganisation).build();

    var url = Url.builder()
      .path("/selections/cities")
      .parameter(CITY, "sverige,borås")
      .sortBy("city,asc")
      .build();

    var response = as(user).getPage(url, CityDto.class);

    assertThat(response.getContent()).isEmpty();
  }

  @Test
  public void noFacilities_WhenFacilityParameterNotIncluded_InSelectionParameters() {
    var userSelection = userSelections.save(subOrganisationUserSelection()
      .selectionParameters(toJsonNode(FACILITIES_JSON_STRING))
      .build());

    var subOrganisation = createSubOrganisation(userSelection);

    given(
      logicalMeter().location(kungsbacka().build()),
      logicalMeter().location(stockholm().build())
    );

    var user = subOrgUser().organisation(subOrganisation).build();

    var url = Url.builder()
      .path("/selections/facilities")
      .parameter(FACILITY, "ext1")
      .sortBy("externalId,asc")
      .build();

    var response = as(user).getPage(url, CityDto.class);

    assertThat(response.getContent()).isEmpty();
  }

  @Test
  public void excludeCities_NotIncluded_ByTheFacilityIds_InSelectionParameters() {
    var userSelection = userSelections.save(subOrganisationUserSelection()
      .selectionParameters(toJsonNode(FACILITIES_JSON_STRING))
      .build());

    var subOrganisation = createSubOrganisation(userSelection);

    given(
      logicalMeter().externalId("demo1").location(kungsbacka().build()),
      logicalMeter().externalId("demo2").location(kungsbacka().build()),
      logicalMeter().externalId("demo3").location(stockholm().build())
    );

    var user = subOrgUser().organisation(subOrganisation).build();

    var url = Url.builder()
      .path("/selections/cities")
      .sortBy("city,asc")
      .build();

    var response = as(user).getPage(url, CityDto.class);

    assertThat(response.getContent()).containsExactly(new CityDto("kungsbacka", "sverige"));
  }

  private Organisation createSubOrganisation(UserSelection userSelection) {
    return organisations.save(Organisation.builderFrom("sub-org")
      .parent(context().defaultOrganisation())
      .selection(userSelection)
      .build()
    );
  }

  private UserSelection.UserSelectionBuilder subOrganisationUserSelection() {
    return UserSelection.builder()
      .id(randomUUID())
      .name("a-user-selection")
      .ownerUserId(context().superAdmin.id)
      .organisationId(context().organisationId())
      .selectionParameters(toJsonNode(CITIES_JSON_STRING));
  }
}
