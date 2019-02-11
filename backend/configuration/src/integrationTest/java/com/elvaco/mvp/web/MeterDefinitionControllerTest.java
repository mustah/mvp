package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeterDefinitionControllerTest extends IntegrationTest {

  @Test
  public void findAll_systemDefinitionsVisibleForUser() {
    List<MeterDefinitionDto> definitionDtos = asUser().getList(
      meterDefinitionsUrl(),
      MeterDefinitionDto.class
    ).getBody();

    List<MeterDefinition> all = meterDefinitions.findAll();
    assertThat(definitionDtos)
      .hasSameSizeAs(all);
  }

  @Test
  public void findAll_systemDefinitionsVisibleForAdmin() {
    List<MeterDefinitionDto> definitionDtos = asAdmin().getList(
      meterDefinitionsUrl(),
      MeterDefinitionDto.class
    ).getBody();

    List<MeterDefinition> all = meterDefinitions.findAll();
    assertThat(definitionDtos)
      .hasSameSizeAs(all);
  }

  @Test
  public void findAll_definitionsVisibleToOwningOrganisation() {
    MeterDefinition meterDefinition
      = given(meterDefinition().medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
      .name("test")
      .organisation(context().organisation()));

    List<MeterDefinitionDto> definitionDtos = asUser().getList(
      meterDefinitionsUrl(),
      MeterDefinitionDto.class
    ).getBody();

    assertThat(definitionDtos)
      .extracting((md) -> md.id)
      .contains(meterDefinition.id);
  }

  @Test
  public void findAll_definitionsNotVisibleToOtherOrganisation() {
    MeterDefinition meterDefinition = given(
      meterDefinition()
        .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
        .name("test")
        .organisation(context().organisation()
        )
    );

    List<MeterDefinitionDto> definitionDtos = asOtherUser().getList(
      meterDefinitionsUrl(),
      MeterDefinitionDto.class
    ).getBody();

    assertThat(definitionDtos)
      .extracting((md) -> md.id)
      .doesNotContain(meterDefinition.id);
  }

  @Test
  public void findAll_definitionsVisibleToSubOrganisation() {
    Organisation subOrg = given(subOrganisation());

    User subOrgUser = given(user().organisation(subOrg));

    MeterDefinition meterDefinition = given(
      meterDefinition()
        .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
        .name("test")
        .organisation(context().organisation())
    );

    List<MeterDefinitionDto> definitionDtos = as(subOrgUser).getList(
      meterDefinitionsUrl(),
      MeterDefinitionDto.class
    ).getBody();

    assertThat(definitionDtos)
      .extracting((md) -> md.id)
      .contains(meterDefinition.id);
  }

  private static Url meterDefinitionsUrl() {
    return Url.builder().path("/meter-definitions").build();
  }
}
