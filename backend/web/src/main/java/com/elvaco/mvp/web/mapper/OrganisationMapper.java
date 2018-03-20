package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.web.dto.OrganisationDto;

import static com.elvaco.mvp.web.util.IdHelper.uuidOf;

public class OrganisationMapper {

  public OrganisationDto toDto(Organisation organisation) {
    return new OrganisationDto(organisation.id.toString(), organisation.name, organisation.slug);
  }

  public Organisation toDomainModel(OrganisationDto organisationDto) {
    return new Organisation(
      uuidOf(organisationDto.id),
      organisationDto.name,
      organisationDto.slug
    );
  }
}
