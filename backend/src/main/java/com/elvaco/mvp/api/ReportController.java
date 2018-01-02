package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.entity.report.ReportEntity;
import com.elvaco.mvp.repository.ReportRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/api/reports")
public class ReportController {

  private final ReportRepository repository;

  @Autowired
  public ReportController(ReportRepository repository) {
    this.repository = repository;
  }

  @RequestMapping
  public List<ReportEntity> allReports() {
    return repository.findAll();
  }
}
