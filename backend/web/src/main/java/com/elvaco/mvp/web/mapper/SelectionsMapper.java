package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.Status;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.SelectionsDto;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class SelectionsMapper {

  public static final List<IdNamedDto> GATEWAY_STATUSES = unmodifiableList(asList(
    new IdNamedDto("ok"),
    new IdNamedDto("warning"),
    new IdNamedDto("fault")
  ));

  public static final List<IdNamedDto> METER_STATUSES = unmodifiableList(
    Stream.of(Status.values())
      .map(value -> value.name)
      .map(IdNamedDto::new)
      .collect(Collectors.toList()));

  public static final List<IdNamedDto> METER_ALARMS = unmodifiableList(asList(
    new IdNamedDto("no error"),
    new IdNamedDto("battery low"),
    new IdNamedDto("flow sensor error (air)"),
    new IdNamedDto("flow sensor error (generic)"),
    new IdNamedDto("flow sensor error (dirty)"),
    new IdNamedDto("leakage"),
    new IdNamedDto("overflow"),
    new IdNamedDto("backflow"),
    new IdNamedDto("forward temperature sensor error"),
    new IdNamedDto("return temperature sensor error"),
    new IdNamedDto("temperature sensor error (generic)"),
    new IdNamedDto("temperature sensor inverted"),
    new IdNamedDto("tamper error"),
    new IdNamedDto("supply voltage error"),
    new IdNamedDto("time for battery change"),
    new IdNamedDto("internal meter error")
  ));

  public SelectionsDto addToDto(Location location, SelectionsDto selectionsDto) {
    // TODO: Even if a meter has an unknown location we should send add it to the selectionsDto
    // however, we don't have a way of sorting of meters with locations equal to null. This method
    // should be implemented before adding unknown locations to the DTO.
    if (location.isKnownAddress()) {
      selectionsDto.addLocation(
        location.getCountry(),
        location.getCity(),
        location.getStreetAddress()
      );
    }
    return selectionsDto;
  }
}
