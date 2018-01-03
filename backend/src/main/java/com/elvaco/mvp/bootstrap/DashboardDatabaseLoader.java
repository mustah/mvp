package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import com.elvaco.mvp.config.H2;
import com.elvaco.mvp.entity.dashboard.DashboardEntity;
import com.elvaco.mvp.repository.jpa.DashboardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@H2
@Component
public class DashboardDatabaseLoader implements CommandLineRunner {

  private final DashboardRepository repository;

  @Autowired
  public DashboardDatabaseLoader(DashboardRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) {
    Stream.of(
      new DashboardEntity("java-backend", "john"),
      new DashboardEntity("java-collection", "doh"),
      new DashboardEntity("metering", "bob")
    )
      .forEach(repository::save);
  }
}
