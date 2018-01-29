package com.elvaco.mvp.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.exception.MeasurementNotFound;
import com.elvaco.mvp.security.MvpUserDetails;
import com.elvaco.mvp.spring.PageableAdapter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestApi("/v1/api/measurements")
public class MeasurementController {

  private final MeasurementUseCases measurementUseCases;
  private final ModelMapper modelMapper;

  @Autowired
  MeasurementController(
    MeasurementUseCases measurementUseCases,
    ModelMapper modelMapper
  ) {
    this.measurementUseCases = measurementUseCases;
    this.modelMapper = modelMapper;
  }

  @GetMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    MvpUserDetails principal = getPrincipal();
    Measurement measurement = measurementUseCases.findById(id);
    if (principal.isSuperAdmin() || principal.isWithinOrganisation(measurement.physicalMeter
                                                                     .organisation)) {
      return toDto(measurement);
    }

    throw new MeasurementNotFound(id);
  }

  @GetMapping
  public org.springframework.data.domain.Page<MeasurementDto> measurements(
    @RequestParam(value = "scale", required = false) String scale,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    Map<String, List<String>> filterParams = new HashMap<>(requestParams);
    PageableAdapter pageableAdapter = new PageableAdapter(pageable);
    Page<Measurement> page;

    MvpUserDetails principal = getPrincipal();
    if (!principal.isSuperAdmin()) {
      Long organisationId = principal.getOrganisationId();
      filterParams.put("organisation", Collections.singletonList(organisationId.toString()));
    }

    if (scale != null) {
      page = measurementUseCases.findAllScaled(
        scale,
        filterParams,
        pageableAdapter
      );

    } else {
      page = measurementUseCases.findAll(filterParams, pageableAdapter);
    }
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(this::toDto);
  }

  private MeasurementDto toDto(Measurement entity) {
    return modelMapper.map(entity, MeasurementDto.class);
  }

  private MvpUserDetails getPrincipal() {
    return (MvpUserDetails) SecurityContextHolder.getContext()
      .getAuthentication()
      .getPrincipal();
  }
}
