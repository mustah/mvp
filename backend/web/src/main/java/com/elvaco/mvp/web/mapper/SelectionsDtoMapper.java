package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.SelectionsDto;
import lombok.experimental.UtilityClass;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class SelectionsDtoMapper {

  public static final List<IdNamedDto> GATEWAY_STATUSES = unmodifiableList(asList(
    new IdNamedDto("ok"),
    new IdNamedDto("warning"),
    new IdNamedDto("fault")
  ));

  public static final List<IdNamedDto> METER_STATUSES = unmodifiableList(
    Stream.of(StatusType.values())
      .map(value -> value.name)
      .map(IdNamedDto::new)
      .collect(toList()));

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

  public static final List<IdNamedDto> MEDIA = unmodifiableList(
    Stream.of(Medium.values())
      .map(medium -> medium.medium)
      .filter(medium -> !medium.equals("Heat, Return temp"))
      .map(IdNamedDto::new)
      .collect(toList())
  );

  public static void addToDto(Location location, SelectionsDto selectionsDto) {
    selectionsDto.addLocation(
      location.getCountryOrUnknown(),
      location.getCityOrUnknown(),
      location.getAddressOrUnknown()
    );
  }
}
