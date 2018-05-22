package com.elvaco.mvp.web.api;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.SelectionsDto;
import com.elvaco.mvp.web.mapper.SelectionsDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@RestApi("/api/v1/selections")
public class SelectionController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final SelectionsDtoMapper selectionsDtoMapper;

  @GetMapping
  public SelectionsDto selections() {
    SelectionsDto selectionsDto = new SelectionsDto();
    logicalMeterUseCases.findAll(new RequestParametersAdapter())
      .forEach(meter -> selectionsDtoMapper.addToDto(meter.location, selectionsDto));
    return selectionsDto;
  }
}
