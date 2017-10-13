package com.elvaco.mvp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elvaco.mvp.entity.report.ReportEntity;
import com.elvaco.mvp.repository.ReportRepository;

@RestApi
public class ReportController {

  private final ReportRepository repository;

  @Autowired
  public ReportController(ReportRepository repository) {
    this.repository = repository;
  }

  @RequestMapping("/reports")
  public List<ReportEntity> allReports() {
    return repository.findAll();
  }
}