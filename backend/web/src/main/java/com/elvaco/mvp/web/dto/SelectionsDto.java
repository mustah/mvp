package com.elvaco.mvp.web.dto;

import java.util.List;

import lombok.experimental.UtilityClass;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@UtilityClass
public class SelectionsDto {

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
}
