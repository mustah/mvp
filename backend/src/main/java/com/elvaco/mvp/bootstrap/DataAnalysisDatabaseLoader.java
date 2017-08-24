package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.entities.dataanalysis.DataAnalysisEntity;
import com.elvaco.mvp.repositories.DataAnalysisRepository;

@Component
public class DataAnalysisDatabaseLoader implements CommandLineRunner {

  private final DataAnalysisRepository repository;

  @Autowired
  public DataAnalysisDatabaseLoader(DataAnalysisRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
      new DataAnalysisEntity("java-backend", "john"),
      new DataAnalysisEntity("java-collection", "doh"),
      new DataAnalysisEntity("metering", "bob"))
      .forEach(repository::save);
  }
}
