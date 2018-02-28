package com.elvaco.mvp.testing.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

abstract class MockRepository<T> {

  private final List<T> entities;

  MockRepository() {
    this(emptyList());
  }

  MockRepository(List<T> initial) {
    this.entities = new ArrayList<>(initial);
  }

  protected abstract Optional<Long> getId(T entity);

  protected abstract T copyWithId(Long id, T entity);

  final T saveMock(T entity) {
    if (getId(entity).isPresent()) {
      entities.set(Math.toIntExact(getId(entity).get()), entity);
    } else {
      entity = copyWithId((long) entities.size(), entity);
      entities.add(entity);
    }
    return entity;
  }

  final Stream<T> filter(Predicate<T> predicate) {
    return entities.stream().filter(predicate);
  }

  final List<T> allMocks() {
    return entities;
  }
}
