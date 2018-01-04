package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import com.elvaco.mvp.config.H2;
import com.elvaco.mvp.entity.report.ReportEntity;
import com.elvaco.mvp.repository.jpa.ReportRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@H2
@Component
public class ReportDatabaseLoader implements CommandLineRunner {

  private final ReportRepository repository;

  @Autowired
  public ReportDatabaseLoader(ReportRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) {
    Stream.of(
      new ReportEntity("Nisse", "validated"),
      new ReportEntity("Freddy", "not fully validated"),
      new ReportEntity("Teddy", "has errors"),
      new ReportEntity("Bear", "with warnings")
    )
      .forEach(repository::save);
  }
}
