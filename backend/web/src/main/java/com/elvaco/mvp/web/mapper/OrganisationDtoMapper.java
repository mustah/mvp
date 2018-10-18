package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

@UtilityClass
public class OrganisationDtoMapper {

  public static OrganisationDto toDto(Organisation organisation) {
    return new OrganisationDto(
      organisation.id,
      organisation.name,
      organisation.slug,
      organisation.parent != null ? toDto(organisation.parent) : null
    );
  }

  public static Organisation toDomainModel(OrganisationDto organisationDto) {
    return new Organisation(
      organisationDto.id != null ? organisationDto.id : randomUUID(),
      organisationDto.name,
      organisationDto.slug,
      organisationDto.name
    );
  }

  public static Organisation toDomainModel(
    Organisation parent,
    SubOrganisationRequestDto requestDto
  ) {
    return new Organisation(
      randomUUID(),
      requestDto.name,
      requestDto.slug,
      requestDto.name,
      parent
    );
  }
}
