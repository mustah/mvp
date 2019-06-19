package com.elvaco.mvp.web;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.Units;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.DisplayQuantityDto;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MediumDto;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;
import com.elvaco.mvp.web.dto.QuantityDto;
import com.elvaco.mvp.web.mapper.MeterDefinitionDtoMapper;
import com.elvaco.mvp.web.mapper.OrganisationDtoMapper;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeterDefinitionControllerTest extends IntegrationTest {

  @Test
  public void findAll_systemDefinitionsVisibleForUser() {
    List<MeterDefinitionDto> definitionDtos = asMvpUser().getList(
      meterDefinitionsUrl(),
      MeterDefinitionDto.class
    ).getBody();

    List<MeterDefinition> all = meterDefinitions.findAll();
    assertThat(definitionDtos)
      .hasSameSizeAs(all);
  }

  @Test
  public void findAll_systemDefinitionsVisibleForAdmin() {
    List<MeterDefinitionDto> definitionDtos = asMvpAdmin().getList(
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
      = given(meterDefinition().medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING)));

    List<MeterDefinitionDto> definitionDtos = asMvpUser().getList(
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
        .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING)));

    var userOnOtherOrganisation = given(organisation(), mvpUser()).getUser();

    List<MeterDefinitionDto> definitionDtos = as(userOnOtherOrganisation).getList(
      meterDefinitionsUrl(),
      MeterDefinitionDto.class
    ).getBody();

    assertThat(definitionDtos)
      .extracting((md) -> md.id)
      .doesNotContain(meterDefinition.id);
  }

  @Test
  public void findAll_definitionsVisibleToSubOrganisation() {
    User subOrgUser = given(subOrganisation(), mvpUser()).getUser();

    MeterDefinition meterDefinition = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING)));

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

    ResponseEntity<ErrorMessageDto> response = asMvpAdmin().post(
      meterDefinitionsUrl(),
      dto,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void create_subOrganisationsCannotOwnDefinitions() {
    Organisation subOrg = given(subOrganisation());

    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(subOrg),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.WATER)),
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
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(context().defaultOrganisation()),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.WATER)),
      false
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpAdmin().post(
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
  public void create_autoApplyOnExistingMeters() {
    var logicalMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING)
      .organisationId(context().organisationId()));

    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(context().defaultOrganisation()),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING)),
      true
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(logicalMeterJpaRepository.findById(logicalMeter.id).get().meterDefinition.id)
      .isEqualTo(response.getBody().id);
  }

  @Test
  public void create_userCannotCreateDefinition() {
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(context().defaultOrganisation()),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.WATER)),
      true
    );

    ResponseEntity<ErrorMessageDto> response = asMvpUser().post(
      meterDefinitionsUrl(),
      meterDefinition,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void create_cannotCreateDefinitionForOtherOrganisation() {
    Organisation otherOrganisation = given(organisation());

    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Collections.emptySet(),
      OrganisationDtoMapper.toDto(otherOrganisation),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.WATER)),
      true
    );

    ResponseEntity<ErrorMessageDto> response = asMvpAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void create_withDisplayQuantities() {
    DisplayQuantityDto displayQuantityDto = new DisplayQuantityDto(
      Quantity.POWER.name,
      false,
      Units.WATT,
      9
    );
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Set.of(displayQuantityDto),
      OrganisationDtoMapper.toDto(context().defaultOrganisation()),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.ELECTRICITY)),
      false
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var dto = response.getBody();
    assertThat(dto.id).isNotNull();
    assertThat(dto).isEqualToIgnoringGivenFields(meterDefinition, "id");
    assertThat(dto.quantities).containsOnly(displayQuantityDto);

    MeterDefinition saved = meterDefinitions.findById(dto.id).get();
    assertThat(saved.name).isEqualTo(meterDefinition.name);
    assertThat(saved.organisation.id).isEqualTo(meterDefinition.organisation.id);
    assertThat(saved.quantities).extracting(q -> q.quantity.name)
      .containsExactly(Quantity.POWER.name);
  }

  @Test
  public void create_measurementsUsesNewMeterDefinition() {
    var start = context().now();
    var logicalMeter = given(logicalMeter()
      .meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));
    given(measurementSeries()
      .forMeter(logicalMeter)
      .startingAt(start)
      .withQuantity(Quantity.ENERGY)
      .withValues(2000, 4000, 12000));
    given(measurementSeries()
      .forMeter(logicalMeter)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(1, 5));

    String url = "/measurements?quantity=Energy,Power"
      + "&logicalMeterId=" + logicalMeter.id
      + "&reportAfter=" + start
      + "&reportBefore=" + start.plusHours(1);
    var measurementSeriesDtos = asMvpUser().getList(url, MeasurementSeriesDto.class).getBody();

    assertThat(measurementSeriesDtos)
      .flatExtracting(dto -> dto.values)
      .extracting(value -> value.value)
      .containsOnly(2000.0, 8000.0, 1.0, 5.0);

    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "Energy as MWh",
      Set.of(new DisplayQuantityDto(Quantity.ENERGY.name, true, Units.MEGAWATT_HOURS, 1)),
      OrganisationDtoMapper.toDto(context().defaultOrganisation()),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING)),
      true
    );

    asMvpAdmin().post(meterDefinitionsUrl(), meterDefinition, MeterDefinitionDto.class);

    measurementSeriesDtos = asMvpUser().getList(url, MeasurementSeriesDto.class).getBody();

    assertThat(measurementSeriesDtos)
      .flatExtracting(dto -> dto.values)
      .extracting(value -> value.value)
      .containsExactly(2.0, 8.0);
  }

  @Test
  public void create_withInvalidDisplayQuantityName() {
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Set.of(new DisplayQuantityDto("Not a valid quantity name", false, Units.WATT, 3)),
      OrganisationDtoMapper.toDto(context().defaultOrganisation()),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.ELECTRICITY)),
      true
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void create_withInvalidDisplayQuantityDecimals() {
    MeterDefinitionDto meterDefinition = new MeterDefinitionDto(
      null,
      "test",
      Set.of(new DisplayQuantityDto(Quantity.POWER.name, false, Units.WATT, -1)),
      OrganisationDtoMapper.toDto(context().defaultOrganisation()),
      getMediumDto(mediumProvider.getByNameOrThrow(Medium.ELECTRICITY)),
      true
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpAdmin().post(
      meterDefinitionsUrl(),
      meterDefinition,
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void update_updateMeterDefinition() {
    var meterDefinition = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
      .quantities(Set.of(new DisplayQuantity(
        Quantity.POWER,
        DisplayMode.READOUT,
        9,
        Units.WATT
      ))));

    DisplayQuantityDto newDisplayQuanitity = new DisplayQuantityDto(
      Quantity.ENERGY.name,
      false,
      Units.KILOWATT_HOURS,
      2
    );
    MeterDefinitionDto newMeterDefinitionDto = MeterDefinitionDtoMapper.toDto(meterDefinition);
    newMeterDefinitionDto.name = "NEW: " + meterDefinition.name;
    newMeterDefinitionDto.quantities = Set.of(newDisplayQuanitity);

    asSuperAdmin().put(
      meterDefinitionsUrl().template(),
      newMeterDefinitionDto,
      MeterDefinitionDto.class
    );

    MeterDefinition saved = meterDefinitions.findById(meterDefinition.id).get();
    assertThat(saved.name).isEqualTo(newMeterDefinitionDto.name);
    assertThat(saved.organisation.id).isEqualTo(newMeterDefinitionDto.organisation.id);
    assertThat(saved.quantities).extracting(q -> q.quantity.name)
      .containsExactly(Quantity.ENERGY.name);
  }

  @Test
  public void delete_systemMeterDefinitionCanNotBeDeleted() {
    var meterDefinition = systemMeterDefinitionProvider.getByMediumOrThrow(
      mediumProvider.getByNameOrThrow(Medium.WATER));

    ResponseEntity<MeterDefinitionDto> response = asSuperAdmin().delete(
      meterDefinitionsUrl("/" + meterDefinition.id).template(),
      MeterDefinitionDto.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void delete_superAdminCanDeleteOrganisationMeterDefinition() {
    var organisation = given(organisation());
    var meterDefinition = given(
      meterDefinition()
        .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
        .organisation(organisation)
    );

    ResponseEntity<MeterDefinitionDto> response = asSuperAdmin().delete(
      meterDefinitionsUrl("/" + meterDefinition.id).template(),
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting(
      md -> md.name,
      md -> md.medium.name,
      md -> md.organisation.id
    ).containsExactly(
      meterDefinition.name,
      meterDefinition.medium.name,
      meterDefinition.organisation.id
    );
  }

  @Test
  public void delete_adminCanDeleteOrganisationMeterDefinition() {
    var meterDefinition = given(meterDefinition()
      .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpAdmin().delete(
      meterDefinitionsUrl("/" + meterDefinition.id).template(),
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void delete_adminCanNotDeleteOrganisationMeterDefinitionForOtherOrganisation() {
    var organisation = given(organisation());
    var meterDefinition = given(
      meterDefinition()
        .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
        .organisation(organisation)
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpAdmin().delete(
      meterDefinitionsUrl("/" + meterDefinition.id).template(),
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void delete_userCanNotDeleteMeterDefinition() {
    var meterDefinition = given(
      meterDefinition()
        .medium(mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING))
    );

    ResponseEntity<MeterDefinitionDto> response = asMvpUser().delete(
      meterDefinitionsUrl("/" + meterDefinition.id).template(),
      MeterDefinitionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void delete_meterDefinitionIsSetToDefaultWhenOrganisationMeterDefinitionIsDeleted() {
    var medium = mediumProvider.getByNameOrThrow(Medium.DISTRICT_HEATING);
    var meterDefinition = given(meterDefinition().medium(medium));

    var logicalMeter = given(logicalMeter().meterDefinition(meterDefinition));

    asSuperAdmin().delete(
      meterDefinitionsUrl("/" + meterDefinition.id).template(),
      MeterDefinitionDto.class
    );

    assertThat(logicalMeterJpaRepository.findById(logicalMeter.id).get().meterDefinition.id)
      .isEqualTo(systemMeterDefinitionProvider.getByMediumOrThrow(medium).id);
  }

  @Test
  public void getMedium() {
    List<MediumDto> mediumDtos = asMvpUser().getList(
      meterDefinitionsUrl("/medium"),
      MediumDto.class
    ).getBody();

    List<Medium> all = mediumProvider.all();
    assertThat(mediumDtos).extracting(dto -> dto.name)
      .containsExactlyElementsOf(all.stream().map(m -> m.name).collect(toList()));
  }

  @Test
  public void getQuantity() {
    List<QuantityDto> quantityDtos = asMvpUser().getList(
      meterDefinitionsUrl("/quantities"),
      QuantityDto.class
    ).getBody();

    List<Quantity> all = quantityProvider.all();
    assertThat(quantityDtos).extracting(dto -> dto.name)
      .containsExactlyElementsOf(all.stream().map(m -> m.name).collect(toList()));
  }

  private IdNamedDto getMediumDto(Medium medium) {
    return new IdNamedDto(medium.id.toString(), medium.name);
  }

  private static Url meterDefinitionsUrl() {
    return Url.builder().path("/meter-definitions").build();
  }

  private static Url meterDefinitionsUrl(String path) {
    return Url.builder().path("/meter-definitions" + path).build();
  }
}
