package com.elvaco.mvp.bootstrap;

import com.elvaco.mvp.config.InMemory;
import com.elvaco.mvp.entity.report.ReportEntity;
import com.elvaco.mvp.repository.ReportRepository;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@InMemory
@Component
public class ReportDatabaseLoader implements CommandLineRunner {

  private final ReportRepository repository;

  @Autowired
  public ReportDatabaseLoader(ReportRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
        new ReportEntity("Nisse", "validated"),
        new ReportEntity("Freddy", "not fully validated"),
        new ReportEntity("Teddy", "has errors"),
        new ReportEntity("Bear", "with warnings"))
        .forEach(repository::save);
  }
}
