package com.elvaco.mvp.web.mapper;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.web.dto.OrganisationDto;

import static java.util.UUID.randomUUID;

public class OrganisationMapper {

  public OrganisationDto toDto(Organisation organisation) {
    return new OrganisationDto(organisation.id.toString(), organisation.name, organisation.code);
  }

  public Organisation toDomainModel(OrganisationDto organisationDto) {
    return new Organisation(
      organisationDto.id != null ? UUID.fromString(organisationDto.id) : randomUUID(),
      organisationDto.name,
      organisationDto.code
    );
  }
}
