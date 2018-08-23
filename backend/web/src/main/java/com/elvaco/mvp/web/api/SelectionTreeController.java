package com.elvaco.mvp.web.api;

import com.elvaco.mvp.core.domainmodels.SelectionTree;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.web.dto.SelectionTreeDto;
import com.elvaco.mvp.web.mapper.SelectionTreeDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;

@RequiredArgsConstructor
@RestApi("/api/v1/selection-tree")
public class SelectionTreeController {

  private final LogicalMeterUseCases logicalMeterUseCases;

  @GetMapping
  public SelectionTreeDto selectionTree(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    SelectionTree selectionTree = new SelectionTree();

    logicalMeterUseCases.selectionTree(requestParametersOf(requestParams))
      .forEach((logicalMeter -> SelectionTreeDtoMapper.addToDto(
        logicalMeter,
        selectionTree
      )));

    return SelectionTreeDtoMapper.toDto(selectionTree);
  }
}
