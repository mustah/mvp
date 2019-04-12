package com.elvaco.mvp.web.security;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.domainmodels.UserSelection.IdNamedDto;
import com.elvaco.mvp.core.domainmodels.UserSelection.SelectionParametersDto;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import org.junit.Test;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.FACILITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class MvpUserDetailsTest {

  @Test
  public void shouldHaveOrganisationIdOfSubOrganisation() {
    var subOrganisationId = randomUUID();
    var userId = randomUUID();
    var subOrganisation = Organisation.builderFrom("Org AB")
      .id(subOrganisationId)
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .id(randomUUID())
        .ownerUserId(userId)
        .organisationId(subOrganisationId)
        .name("selection")
        .selectionParameters(toJsonNode(FACILITIES_JSON_STRING))
        .build()
      )
      .build();

    var authenticatedUser = authenticatedUserFrom(userBuilder()
      .id(userId)
      .organisation(subOrganisation));

    var subOrganisationParameters = authenticatedUser.subOrganisationParameters();
    assertThat(subOrganisationParameters.getParentOrganisationId()).isEqualTo(MARVEL.getId());
    assertThat(subOrganisationParameters.getOrganisationId()).isEqualTo(subOrganisationId);
  }

  @Test
  public void shouldHaveOrganisationIdWhenUserDoesNotBelongToSubOrganisation() {
    var organisation = Organisation.builderFrom("Org AB").build();

    var authenticatedUser = authenticatedUserFrom(userBuilder().organisation(organisation));

    var subOrganisationParameters = authenticatedUser.subOrganisationParameters();
    assertThat(subOrganisationParameters.getOrganisationId()).isEqualTo(organisation.getId());
    assertThat(subOrganisationParameters.getParentOrganisationId()).isNull();
  }

  @Test
  public void parentOrganisationDoesNotHaveSelectionParameters() {
    var organisation = Organisation.builderFrom("Org AB").build();

    var authenticatedUser = authenticatedUserFrom(userBuilder().organisation(organisation));

    assertThat(authenticatedUser.subOrganisationParameters().selectionParameters()).isNotPresent();
  }

  @Test
  public void subOrganisationHasSelectionParametersWithOneFacilityId() {
    var organisation = Organisation.builderFrom("Org AB")
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .selectionParameters(toJsonNode(FACILITIES_JSON_STRING))
        .build())
      .build();

    var authenticatedUser = authenticatedUserFrom(userBuilder().organisation(organisation));

    assertThat(authenticatedUser.subOrganisationParameters().selectionParameters().get())
      .isEqualTo(new SelectionParametersDto(
        List.of(new IdNamedDto("demo1", "demo1"), new IdNamedDto("demo2", "demo2")),
        null
      ));
  }

  @Test
  public void subOrganisationHasSelectionParametersWithTwoCities() {
    var organisation = Organisation.builderFrom("Org AB")
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .selectionParameters(toJsonNode(CITIES_JSON_STRING))
        .build())
      .build();

    var authenticatedUser = authenticatedUserFrom(userBuilder().organisation(organisation));

    assertThat(authenticatedUser.subOrganisationParameters().selectionParameters().get())
      .isEqualTo(new SelectionParametersDto(
        null,
        List.of(
          new IdNamedDto("sverige,kungsbacka", "kungsbacka"),
          new IdNamedDto("sverige,stockholm", "stockholm")
        )
      ));
  }

  private static MvpUserDetails authenticatedUserFrom(UserBuilder user) {
    return new MvpUserDetails(user.build(), randomUUID().toString());
  }
}
