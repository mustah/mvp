package com.elvaco.mvp.web.api;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.exception.OrganisationNotFound;
import com.elvaco.mvp.web.mapper.OrganisationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static java.util.stream.Collectors.toList;

@RestApi("/v1/api/organisations")
public class OrganisationController {

  private final OrganisationUseCases organisationUseCases;
  private final OrganisationMapper organisationMapper;

  @Autowired
  OrganisationController(
    OrganisationUseCases organisationUseCases,
    OrganisationMapper organisationMapper
  ) {
    this.organisationUseCases = organisationUseCases;
    this.organisationMapper = organisationMapper;
  }

  @GetMapping
  public List<OrganisationDto> allOrganisations() {
    return organisationUseCases.findAll()
      .stream()
      .map(organisationMapper::toDto)
      .collect(toList());
  }

  @GetMapping("{id}")
  public OrganisationDto organisationById(@PathVariable Long id) {
    return organisationUseCases.findById(id)
      .map(organisationMapper::toDto)
      .orElseThrow(() -> new OrganisationNotFound(id));
  }

  @PostMapping
  public ResponseEntity<OrganisationDto> createOrganisation(
    @RequestBody OrganisationDto organisation
  ) {
    return organisationUseCases.create(organisationMapper.toDomainModel(organisation))
      .map(organisationMapper::toDto)
      .map(ResponseEntity.status(HttpStatus.CREATED)::body)
      .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  @PutMapping
  public OrganisationDto updateOrganisation(@RequestBody OrganisationDto organisation) {
    return organisationUseCases.update(organisationMapper.toDomainModel(organisation))
      .map(organisationMapper::toDto)
      .orElseThrow(() -> new OrganisationNotFound(organisation.id));
  }

  @DeleteMapping("{id}")
  public OrganisationDto deleteOrganisation(@PathVariable Long id) {
    Organisation organisation = organisationUseCases.findById(id)
      .orElseThrow(() -> new OrganisationNotFound(id));
    // TODO delete should actually not remove the entity, just mark it as deleted.
    organisationUseCases.delete(organisation);
    return organisationMapper.toDto(organisation);
  }
}
