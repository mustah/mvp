package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.UserSelectionUseCases;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;
import com.elvaco.mvp.web.exception.OrganisationNotFound;
import com.elvaco.mvp.web.exception.UserSelectionNotFound;
import com.elvaco.mvp.web.mapper.OrganisationDtoMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDto;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@RestApi("/api/v1/organisations")
public class OrganisationController {

  private final OrganisationUseCases organisationUseCases;
  private final UserSelectionUseCases selections;

  @GetMapping
  public List<OrganisationDto> allOrganisations() {
    return organisationUseCases.findAll().stream()
      .map(OrganisationDtoMapper::toDto)
      .collect(toList());
  }

  @GetMapping("{id}")
  public OrganisationDto organisationById(@PathVariable UUID id) {
    return organisationUseCases.findById(id)
      .map(OrganisationDtoMapper::toDto)
      .orElseThrow(() -> new OrganisationNotFound(id));
  }

  @PostMapping
  public ResponseEntity<OrganisationDto> createOrganisation(
    @RequestBody OrganisationDto organisation
  ) {
    OrganisationDto responseModel = toDto(organisationUseCases.create(toDomainModel(organisation)));
    return ResponseEntity.status(HttpStatus.CREATED).body(responseModel);
  }

  @PutMapping
  public OrganisationDto updateOrganisation(@RequestBody OrganisationDto organisation) {
    return toDto(organisationUseCases.update(toDomainModel(organisation)));
  }

  @DeleteMapping("{id}")
  public OrganisationDto deleteOrganisation(@PathVariable UUID id) {
    Organisation organisation = organisationUseCases.findById(id)
      .orElseThrow(() -> new OrganisationNotFound(id));
    // TODO delete should actually not remove the entity, just mark it as deleted.
    organisationUseCases.delete(organisation);
    return toDto(organisation);
  }

  @PostMapping("{id}/sub-organisations")
  public ResponseEntity<OrganisationDto> createSubOrganisation(
    @PathVariable UUID id,
    @RequestBody SubOrganisationRequestDto requestDto
  ) {
    UserSelection selection = selections.findByIdForCurrentUser(requestDto.selectionId)
      .orElseThrow(() -> new UserSelectionNotFound(requestDto.selectionId));

    return organisationUseCases.findById(id)
      .map(parent -> OrganisationDtoMapper.toDomainModel(parent, selection, requestDto))
      .map(organisationUseCases::create)
      .map(OrganisationDtoMapper::toDto)
      .map(subOrganisationDto -> ResponseEntity.status(HttpStatus.CREATED).body(subOrganisationDto))
      .orElseThrow(() -> new OrganisationNotFound(id));
  }
}
