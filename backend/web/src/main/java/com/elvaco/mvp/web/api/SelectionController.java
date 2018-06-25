package com.elvaco.mvp.web.api;

import java.util.Map;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LocationUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.SelectionsDto;
import com.elvaco.mvp.web.mapper.LocationDtoMapper;
import com.elvaco.mvp.web.mapper.SelectionsDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;

@RequiredArgsConstructor
@RestApi("/api/v1/selections")
public class SelectionController {

  private final PhysicalMeterUseCases physicalMeterUseCases;

  private final LogicalMeterUseCases logicalMeterUseCases;

  private final LocationUseCases locationUseCases;

  private final GatewayUseCases gatewayUseCases;

  @GetMapping
  public SelectionsDto selections() {
    SelectionsDto selectionsDto = new SelectionsDto();
    logicalMeterUseCases.findAll(new RequestParametersAdapter())
      .forEach(meter -> SelectionsDtoMapper.addToDto(meter, selectionsDto));
    return selectionsDto;
  }

  @GetMapping("facilities")
  public org.springframework.data.domain.Page<IdNamedDto> facilities(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);

    Page<IdNamedDto> page = physicalMeterUseCases.findAll(parameters, adapter)
      .map(value -> value.externalId)
      .map(IdNamedDto::new);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
  }

  @GetMapping("locations")
  public org.springframework.data.domain.Page<LocationDto> locations(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {

    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<Location> page = locationUseCases.findAll(parameters, adapter);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(LocationDtoMapper::toLocationDto);
  }

  @GetMapping("secondaryAddresses")
  public org.springframework.data.domain.Page<IdNamedDto> secondaryAddresses(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);

    Page<IdNamedDto> page = physicalMeterUseCases.findAll(parameters, adapter)
      .map(value -> value.address)
      .map(IdNamedDto::new);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
  }

  @GetMapping("gateway/serials")
  public org.springframework.data.domain.Page<IdNamedDto> gatewaySerial(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);

    Page<IdNamedDto> page = gatewayUseCases.findAll(parameters, adapter)
      .map(value -> value.serial)
      .map(IdNamedDto::new);

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
  }
}
