package com.elvaco.mvp.web.mapper;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;
import org.junit.Test;

import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDto;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationDtoMapperTest {

  @Test
  public void toDomainModel_subOrganisation() {
    Organisation parent = new Organisation(
      UUID.randomUUID(),
      "parent",
      "parent-slug",
      "parent-external-id"
    );

    SubOrganisationRequestDto subOrganisationRequest = new SubOrganisationRequestDto(
      "sub",
      "sub-slug",
      UUID.randomUUID()
    );

    assertThat(toDomainModel(parent, subOrganisationRequest))
      .isEqualToIgnoringGivenFields(
        new Organisation(
          null, "sub", "sub-slug", "sub", parent
        ),
        "id"
      );
  }

  @Test
  public void toDto_noSubOrganisation() {
    UUID id = UUID.randomUUID();
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
    UUID id = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
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
      )
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
        )
      )
    );
  }
}
