package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OrganisationDtoMapper {

  public static OrganisationDto toDto(Organisation organisation) {
    return new OrganisationDto(
      organisation.id,
      organisation.name,
      organisation.slug,
      organisation.parent != null ? toDto(organisation.parent) : null,
      organisation.selection != null ? organisation.selection.id : null
    );
  }

  public static IdNamedDto toIdNamedDto(Organisation organisation) {
    return new IdNamedDto(organisation.id.toString(), organisation.name);
  }

  public static Organisation toDomainModel(OrganisationDto organisationDto) {
    return Organisation.of(organisationDto.name, organisationDto.id);
  }

  public static Organisation toDomainModel(
    Organisation parent,
    UserSelection selection,
    SubOrganisationRequestDto requestDto
  ) {
    return Organisation.subOrganisation(requestDto.name, parent, selection).build();
  }
}
