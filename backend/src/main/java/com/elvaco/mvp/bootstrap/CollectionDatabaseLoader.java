package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.entities.collection.CollectionEntity;
import com.elvaco.mvp.repositories.CollectionRepository;

@Component
public class CollectionDatabaseLoader implements CommandLineRunner {

  private final CollectionRepository repository;

  @Autowired
  public CollectionDatabaseLoader(CollectionRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
      new CollectionEntity("java-backend", "john"),
      new CollectionEntity("java-collection", "doh"),
      new CollectionEntity("metering", "bob"))
      .forEach(repository::save);
  }
}
