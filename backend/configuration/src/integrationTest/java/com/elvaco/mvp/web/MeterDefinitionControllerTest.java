package com.elvaco.mvp.web;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.Units;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;
import com.elvaco.mvp.web.dto.QuantityDto;
import com.elvaco.mvp.web.mapper.OrganisationDtoMapper;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

  @Test
  public void create_invalidInputObject() {
    MeterDefinitionDto dto = new MeterDefinitionDto();

    ResponseEntity<ErrorMessageDto> response = asAdmin().post(
      meterDefinitionsUrl(),
      dto,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void create_subOrganisationsCannotOwnDefinitions() {
    Organisation subOrg = given(subOrganisation());

    Medium medium = mediumProvider.getByNameOrThrow(Medium.WATER);
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(subOrg),
      new IdNamedDto(medium.id.toString(), medium.name),
      true
    );

    ResponseEntity<ErrorMessageDto> response = asSuperAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().status).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  public void create_adminCreatesMeterDefinition() {
    Medium medium = mediumProvider.getByNameOrThrow(Medium.WATER);
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(context().organisation()),
      new IdNamedDto(medium.id.toString(), medium.name),
      true
    );

    ResponseEntity<MeterDefinitionDto> response = asAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var dto = response.getBody();
    assertThat(dto.id).isNotNull();
    assertThat(dto).isEqualToIgnoringGivenFields(meterDefinition, "id");
  }

  @Test
  public void create_userCannotCreateDefinition() {
    Medium medium = mediumProvider.getByNameOrThrow(Medium.WATER);
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(context().organisation()),
      new IdNamedDto(medium.id.toString(), medium.name),
      true
    );

    ResponseEntity<ErrorMessageDto> response = asUser().post(
      meterDefinitionsUrl(),
      meterDefinition,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void create_cannotCreateDefinitionForOtherOrganisation() {
    Medium medium = mediumProvider.getByNameOrThrow(Medium.WATER);
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(context().organisation2()),
      new IdNamedDto(medium.id.toString(), medium.name),
      true
    );

    ResponseEntity<ErrorMessageDto> response = asAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void create_withDisplayQuantities() {
    Medium medium = mediumProvider.getByNameOrThrow(Medium.ELECTRICITY);
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Set.of(new QuantityDto(Quantity.POWER.name, false, Units.WATT, 9)),
      OrganisationDtoMapper.toDto(context().organisation()),
      new IdNamedDto(medium.id.toString(), medium.name),
      true
    );

    ResponseEntity<MeterDefinitionDto> response = asAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var dto = response.getBody();
    assertThat(dto.id).isNotNull();
    assertThat(dto).isEqualToIgnoringGivenFields(meterDefinition, "id");
  }

  @Test
  public void create_withInvalidDisplayQuantityName() {
    Medium medium = mediumProvider.getByNameOrThrow(Medium.ELECTRICITY);
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Set.of(new QuantityDto("Not a valid quantity name", false, Units.WATT, 3)),
      OrganisationDtoMapper.toDto(context().organisation()),
      new IdNamedDto(medium.id.toString(), medium.name),
      true
    );

    ResponseEntity<MeterDefinitionDto> response = asAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void create_withInvalidDisplayQuantityDecimals() {
    Medium medium = mediumProvider.getByNameOrThrow(Medium.ELECTRICITY);
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Set.of(new QuantityDto(Quantity.POWER.name, false, Units.WATT, -1)),
      OrganisationDtoMapper.toDto(context().organisation()),
      new IdNamedDto(medium.id.toString(), medium.name),
      true
    );

    ResponseEntity<MeterDefinitionDto> response = asAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  private static Url meterDefinitionsUrl() {
    return Url.builder().path("/meter-definitions").build();
  }
}
