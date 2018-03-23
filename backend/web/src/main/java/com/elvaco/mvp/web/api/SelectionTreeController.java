package com.elvaco.mvp.web.api;

import java.util.Map;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.SelectionTree;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.SelectionTreeDto;
import com.elvaco.mvp.web.mapper.SelectionTreeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestApi("/v1/api/selection-tree")
public class SelectionTreeController {

  private final LogicalMeterUseCases logicalMeterUseCases;
  private final SelectionTreeMapper selectionTreeMapper;

  @Autowired
  public SelectionTreeController(
    LogicalMeterUseCases logicalMeterUseCases,
    SelectionTreeMapper selectionTreeMapper
  ) {
    this.logicalMeterUseCases = logicalMeterUseCases;
    this.selectionTreeMapper = selectionTreeMapper;
  }

  @GetMapping
  public SelectionTreeDto selectionTree(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters parameters = RequestParametersAdapter.of(requestParams).setAll(pathVars);
    SelectionTree selectionTree = new SelectionTree();
    logicalMeterUseCases.findAll(parameters)
      .forEach((logicalMeter -> selectionTreeMapper.addToDto(
        logicalMeter,
        selectionTree
      )));

    return selectionTreeMapper.toDto(selectionTree);
  }
}
