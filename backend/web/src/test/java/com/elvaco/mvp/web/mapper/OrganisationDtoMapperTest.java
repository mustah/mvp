package com.elvaco.mvp.web.mapper;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDto;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class OrganisationDtoMapperTest {

  @Test
  public void toDomainModel_subOrganisation() {
    Organisation parent = Organisation.of("parent slug");

    UserSelection selection = UserSelection.builder()
      .id(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(randomUUID())
      .name("selection")
      .selectionParameters(createJsonNode())
      .build();
    SubOrganisationRequestDto subOrganisationRequest = new SubOrganisationRequestDto(
      "sub slug",
      "sub-slug",
      randomUUID()
    );

    assertThat(toDomainModel(
      parent,
      selection,
      subOrganisationRequest
    )).isEqualToIgnoringGivenFields(
      Organisation.subOrganisation("sub slug", parent, selection)
        .parent(parent)
        .selection(selection)
        .build(),
      "id"
    );
  }

  @Test
  public void toDto_noSubOrganisation() {
    UUID id = randomUUID();
    Organisation organisation = Organisation.of("organisation slug", id);

    assertThat(toDto(organisation)).isEqualTo(
      new OrganisationDto(id, "organisation slug")
    );
  }

  @Test
  public void toDto_withSubOrganisation_sub() {
    var id = randomUUID();
    var parentId = randomUUID();
    var selection = UserSelection.builder()
      .id(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(randomUUID())
      .name("selection")
      .selectionParameters(createJsonNode())
      .build();
    var parent = Organisation.of("parent slug", parentId);
    var organisation = Organisation.subOrganisation("sub slug", parent, selection)
      .id(id)
      .build();

    assertThat(toDto(organisation)).isEqualTo(
      new OrganisationDto(
        id,
        "sub slug",
        "sub-slug",
        null,
        new OrganisationDto(parentId, "parent slug"),
        selection.id
      )
    );
  }

  private JsonNode createJsonNode() {
    try {
      return OBJECT_MAPPER.readTree("{\"city\": \"Perstorp\"}");
    } catch (Exception ex) {
      fail("Failed to create JSON node", ex);
      throw new RuntimeException(ex);
    }
  }
}
