package com.elvaco.mvp.web.api;

import java.util.Map;

import com.elvaco.mvp.adapters.spring.PageableAdapter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LocationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.CityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static com.elvaco.mvp.web.dto.SelectionsDto.GATEWAY_STATUSES;
import static com.elvaco.mvp.web.dto.SelectionsDto.MEDIA;
import static com.elvaco.mvp.web.dto.SelectionsDto.METER_ALARMS;
import static com.elvaco.mvp.web.dto.SelectionsDto.METER_STATUSES;

@RequiredArgsConstructor
@RestApi("/api/v1/selections")
public class SelectionController {

  private final PhysicalMeterUseCases physicalMeterUseCases;
  private final LocationUseCases locationUseCases;
  private final GatewayUseCases gatewayUseCases;

  @GetMapping("gateway-statuses")
  public org.springframework.data.domain.Page<IdNamedDto> gatewayStatuses() {
    return new PageImpl<>(GATEWAY_STATUSES);
  }

  @GetMapping("meter-statuses")
  public org.springframework.data.domain.Page<IdNamedDto> meterStatuses() {
    return new PageImpl<>(METER_STATUSES);
  }

  @GetMapping("meter-alarms")
  public org.springframework.data.domain.Page<IdNamedDto> meterAlarms() {
    return new PageImpl<>(METER_ALARMS);
  }

  @GetMapping("media")
  public org.springframework.data.domain.Page<IdNamedDto> media() {
    return new PageImpl<>(MEDIA);
  }

  @GetMapping("cities")
  public org.springframework.data.domain.Page<CityDto> cities(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);

    Page<CityDto> page = locationUseCases.findAllCities(parameters, adapter)
      .map(city -> new CityDto(city.name, city.country));

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
  }

  @GetMapping("addresses")
  public org.springframework.data.domain.Page<AddressDto> addresses(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    RequestParameters parameters = requestParametersOf(requestParams).setAll(pathVars);
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<AddressDto> page = locationUseCases.findAllAddresses(parameters, adapter)
      .map(address -> new AddressDto(address.country, address.city, address.street));

    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
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

  @GetMapping("secondary-addresses")
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

  @GetMapping("gateway-serials")
  public org.springframework.data.domain.Page<IdNamedDto> gatewaySerials(
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
