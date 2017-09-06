package com.elvaco.mvp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elvaco.mvp.entity.dataanalysis.DataAnalysisEntity;
import com.elvaco.mvp.repository.DataAnalysisRepository;

@RestApi
public class DataAnalysisController {

  private final DataAnalysisRepository repository;

  @Autowired
  public DataAnalysisController(DataAnalysisRepository repository) {
    this.repository = repository;
  }

  @RequestMapping("/data-analysis")
  public List<DataAnalysisEntity> dataAnalysis() {
    return repository.findAll();
  }
}
