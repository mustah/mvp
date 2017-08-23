package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.entities.dashboard.Dashboard;
import com.elvaco.mvp.repositories.DashboardRepository;

@Component
public class DashboardDatabaseLoader implements CommandLineRunner {

  private final DashboardRepository repository;

  @Autowired
  public DashboardDatabaseLoader(DashboardRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
      new Dashboard("java-backend", "john"),
      new Dashboard("java-collection", "doh"),
      new Dashboard("metering", "bob"))
      .forEach(repository::save);
  }
}
