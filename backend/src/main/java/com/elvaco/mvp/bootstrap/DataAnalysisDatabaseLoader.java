package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.entity.dataanalysis.DataAnalysisEntity;
import com.elvaco.mvp.repository.DataAnalysisRepository;

@Component
@Profile("default")
public class DataAnalysisDatabaseLoader implements CommandLineRunner {

  private final DataAnalysisRepository repository;

  @Autowired
  public DataAnalysisDatabaseLoader(DataAnalysisRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
      new DataAnalysisEntity("Nisse", "validated"),
      new DataAnalysisEntity("Freddy", "not fully validated"),
      new DataAnalysisEntity("Teddy", "has errors"),
      new DataAnalysisEntity("Bear", "with warnings"))
      .forEach(repository::save);
  }
}
