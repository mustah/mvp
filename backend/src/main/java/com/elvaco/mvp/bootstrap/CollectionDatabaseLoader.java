package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import com.elvaco.mvp.config.H2;
import com.elvaco.mvp.entity.collection.CollectionEntity;
import com.elvaco.mvp.repository.jpa.CollectionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@H2
@Component
public class CollectionDatabaseLoader implements CommandLineRunner {

  private final CollectionRepository repository;

  @Autowired
  public CollectionDatabaseLoader(CollectionRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) {
    Stream.of(
      new CollectionEntity("java-backend", "john"),
      new CollectionEntity("java-collection", "doh"),
      new CollectionEntity("metering", "bob")
    )
      .forEach(repository::save);
  }
}
