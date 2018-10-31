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
    Organisation parent = new Organisation(
      randomUUID(),
      "parent",
      "parent-slug",
      "parent-external-id"
    );

    UserSelection selection = UserSelection.builder()
      .id(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(randomUUID())
      .name("selection")
      .selectionParameters(createJsonNode())
      .build();
    SubOrganisationRequestDto subOrganisationRequest = new SubOrganisationRequestDto(
      "sub",
      "sub-slug",
      randomUUID()
    );

    assertThat(toDomainModel(
      parent,
      selection,
      subOrganisationRequest
    )).isEqualToIgnoringGivenFields(
      new Organisation(
        null, "sub", "sub-slug", "sub", parent, selection
      ),
      "id"
    );
  }

  @Test
  public void toDto_noSubOrganisation() {
    UUID id = randomUUID();
    Organisation organisation = new Organisation(
      id,
      "organisation",
      "organisation-slug",
      "organisation-external-id"
    );

    assertThat(toDto(organisation)).isEqualTo(
      new OrganisationDto(
        id,
        "organisation",
        "organisation-slug"
      )
    );
  }

  @Test
  public void toDto_withSubOrganisation_sub() {
    UUID id = randomUUID();
    UUID parentId = randomUUID();
    UserSelection selection = UserSelection.builder()
      .id(randomUUID())
      .ownerUserId(randomUUID())
      .organisationId(randomUUID())
      .name("selection")
      .selectionParameters(createJsonNode())
      .build();
    Organisation organisation = new Organisation(
      id,
      "sub",
      "sub-slug",
      "sub-external-id",
      new Organisation(
        parentId,
        "parent",
        "parent-slug",
        "parent-external-id"
      ),
      selection
    );

    assertThat(toDto(organisation)).isEqualTo(
      new OrganisationDto(
        id,
        "sub",
        "sub-slug",
        new OrganisationDto(
          parentId,
          "parent",
          "parent-slug"
        ),
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
