package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.web.dto.OrganisationDto;

public class OrganisationMapper {

  public OrganisationDto toDto(Organisation organisation) {
    return new OrganisationDto(organisation.id, organisation.name, organisation.code);
  }

  public Organisation toDomainModel(OrganisationDto organisationDto) {
    return new Organisation(
      organisationDto.id,
      organisationDto.name,
      organisationDto.code
    );
  }
}
