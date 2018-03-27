package com.elvaco.mvp.web.api;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.SelectionsDto;
import com.elvaco.mvp.web.mapper.SelectionsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@RestApi("/api/v1/selections")
public class SelectionController {
  private final LogicalMeterUseCases logicalMeterUseCases;
  private final SelectionsMapper selectionsMapper;

  @Autowired
  SelectionController(
    LogicalMeterUseCases logicalMeterUseCases,
    SelectionsMapper selectionsMapper
  ) {
    this.selectionsMapper = selectionsMapper;
    this.logicalMeterUseCases = logicalMeterUseCases;
  }

  @GetMapping
  public SelectionsDto selections() {
    SelectionsDto selectionsDto = new SelectionsDto();
    logicalMeterUseCases.findAll(new RequestParametersAdapter())
      .forEach(meter -> selectionsMapper.addToDto(meter.location, selectionsDto));
    return selectionsDto;
  }
}
