package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.web.dto.OrganisationDto;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganisationMapper {

  private final ModelMapper modelMapper;

  @Autowired
  public OrganisationMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public OrganisationDto toDto(Organisation organisation) {
    return modelMapper.map(organisation, OrganisationDto.class);
  }

  public Organisation toDomainModel(OrganisationDto organisationDto) {
    return new Organisation(
      organisationDto.id,
      organisationDto.name,
      organisationDto.code
    );
  }

}
