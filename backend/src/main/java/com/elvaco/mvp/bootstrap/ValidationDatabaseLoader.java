package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.config.InMemory;
import com.elvaco.mvp.entity.validation.ValidationEntity;
import com.elvaco.mvp.repository.ValidationRepository;

@InMemory
@Component
public class ValidationDatabaseLoader implements CommandLineRunner {

  private final ValidationRepository repository;

  @Autowired
  public ValidationDatabaseLoader(ValidationRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
      new ValidationEntity("John", "awesome"),
      new ValidationEntity("Clark", "no"),
      new ValidationEntity("Tony", "mythical"))
      .forEach(repository::save);
  }
}
