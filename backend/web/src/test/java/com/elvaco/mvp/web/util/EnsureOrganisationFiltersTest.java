package com.elvaco.mvp.web.util;

import java.util.UUID;
import java.util.stream.Stream;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.web.security.MvpUserDetails;

import org.junit.Test;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.FACILITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class EnsureOrganisationFiltersTest {

  private static final String[] CITIES = new String[] {"sverige,kungsbacka", "sverige,stockholm"};
  private static final String[] FACILITIES = {"demo1", "demo2"};

  @Test
  public void whenUserBelongsToParentOrganisation_DoNotApplyImplicitUserSelection() {
    var organisation = Organisation.builderFrom("Org AB").build();

    var parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(organisation));

    assertThat(parameters.implicitParameters()).isNotPresent();
  }

  @Test
  public void shouldNotReplaceParameterValues_WhenNoSelectionParameters() {
    var organisation = Organisation.builderFrom("Org AB").build();

    var parameters = new RequestParametersAdapter()
      .add(FACILITY, "c1")
      .add(FACILITY, "c2")
      .add(CITY, "sverige,kungsbacka")
      .ensureOrganisationFilters(authenticatedUser(organisation));

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("c1", "c2");
    assertThat(parameters.getValues(CITY)).containsExactly("sverige,kungsbacka");
    assertThat(parameters.implicitParameters()).isNotPresent();
  }

  @Test
  public void shouldJustAddFacilitiesFromSelectionParameters() {
    var parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(makeSubOrganisationWith(FACILITIES_JSON_STRING)))
      .implicitParameters()
      .get();

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder(FACILITIES);
  }

  @Test
  public void shouldJustAddCitiesFromSelectionParameters() {
    var parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(makeSubOrganisationWith(CITIES_JSON_STRING)))
      .implicitParameters()
      .get();

    assertThat(parameters.getValues(CITY)).containsExactlyInAnyOrder(CITIES);
  }

  @Test
  public void implicitParameters_ContainsOnlySelectionParameterFacilities() {
    var subOrgUser = authenticatedUser(makeSubOrganisationWith(FACILITIES_JSON_STRING));
    var parameters = new RequestParametersAdapter()
      .add(FACILITY, "demo3")
      .ensureOrganisationFilters(subOrgUser);

    assertThat(parameters.getValues(FACILITY)).containsExactly("demo3");
    assertThat(parameters.implicitParameters().get().getValues(FACILITY))
      .containsExactlyInAnyOrder(FACILITIES);
  }

  @Test
  public void implicityParameters_ContainsOnlySelectionParameterCities() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,lund")
      .ensureOrganisationFilters(authenticatedUser(makeSubOrganisationWith(CITIES_JSON_STRING)));

    assertThat(parameters.getValues(CITY)).containsExactly("sverige,lund");
    assertThat(parameters.implicitParameters().get().getValues(CITY))
      .containsExactlyInAnyOrder(CITIES);
  }

  @Test
  public void requestedOrganisations_WhenSuperAdmin() {
    var interestingOrganisations = Stream.of(randomUUID(), randomUUID())
      .map(UUID::toString)
      .collect(toList());
    var usersOrganisation = Organisation.builder()
      .slug(randomUUID().toString())
      .build();
    var parameters = new RequestParametersAdapter()
      .setAll(ORGANISATION, interestingOrganisations)
      .ensureOrganisationFilters(authenticatedSuperAdmin(usersOrganisation));

    assertThat(parameters.getValues(ORGANISATION)).containsAll(interestingOrganisations);
  }

  @Test
  public void requestedOrganisations_WhenUser() {
    var organisationsToSpyOn = Stream.of(randomUUID(), randomUUID())
      .map(UUID::toString)
      .collect(toList());
    var usersOrganisation = Organisation.builder()
      .slug(randomUUID().toString())
      .build();
    var parameters = new RequestParametersAdapter()
      .setAll(ORGANISATION, organisationsToSpyOn)
      .ensureOrganisationFilters(authenticatedUser(usersOrganisation));

    assertThat(parameters.getValues(ORGANISATION)).containsExactly(usersOrganisation.id.toString());
  }

  @Test
  public void doNotIncludesFacilities_WhenSelectionParametersAreEmpty() {
    var subOrgUser = authenticatedUser(makeSubOrganisationWith("{\"facilities\": []}"));
    var parameters = new RequestParametersAdapter()
      .add(FACILITY, "c1")
      .add(FACILITY, "c2")
      .ensureOrganisationFilters(subOrgUser);

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("c1", "c2");
    assertThat(parameters.implicitParameters().get().getValues(FACILITY)).isEmpty();
  }

  @Test
  public void doNotIncludesCities_WhenSelectionParametersAreEmpty() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,stockholm")
      .add(CITY, "sverige,kungsbacka")
      .ensureOrganisationFilters(authenticatedUser(makeSubOrganisationWith("{\"cities\": []}")));

    assertThat(parameters.getValues(CITY)).containsExactlyInAnyOrder(CITIES);
    assertThat(parameters.implicitParameters().get().getValues(CITY)).isEmpty();
  }

  @Test
  public void includesUnknown_AndHasNoImplicitCitiesWhenSelectionCitiesAreEmpty() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "unknown,unknown")
      .ensureOrganisationFilters(authenticatedUser(makeSubOrganisationWith("{\"cities\": []}")));

    assertThat(parameters.getValues(CITY)).containsExactly("unknown,unknown");
    assertThat(parameters.implicitParameters().get().getValues(CITY)).isEmpty();
  }

  @Test
  public void hasNoImplicitParameters() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,osby")
      .ensureOrganisationFilters(authenticatedUser(Organisation.builderFrom("Org AB").build()))
      .implicitParameters();

    assertThat(parameters).isNotPresent();
  }

  @Test
  public void hasImplicitParameters() {
    var subOrgParameters = new RequestParametersAdapter()
      .add(CITY, "sverige,osby")
      .ensureOrganisationFilters(authenticatedUser(makeSubOrganisationWith(CITIES_JSON_STRING)))
      .implicitParameters();

    assertThat(subOrgParameters.get().getValues(CITY)).containsExactlyInAnyOrder(CITIES);
  }

  private static MvpUserDetails authenticatedSuperAdmin(Organisation organisation) {
    return new MvpUserDetails(
      userBuilder().organisation(organisation).asSuperAdmin().build(),
      randomUUID().toString()
    );
  }

  private static MvpUserDetails authenticatedUser(Organisation organisation) {
    return new MvpUserDetails(
      userBuilder().organisation(organisation).build(),
      randomUUID().toString()
    );
  }

  private static Organisation makeSubOrganisationWith(String jsonString) {
    return Organisation.builderFrom("Org AB")
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .selectionParameters(toJsonNode(jsonString))
        .build())
      .build();
  }
}
