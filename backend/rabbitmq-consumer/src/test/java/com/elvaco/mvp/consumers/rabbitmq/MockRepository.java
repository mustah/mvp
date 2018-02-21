package com.elvaco.mvp.consumers.rabbitmq;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

abstract class MockRepository<T> {
  private final List<T> entities;

  MockRepository() {
    this.entities = new ArrayList<>();
  }

  abstract Optional<Long> getId(T entity);

  abstract T copyWithId(Long id, T entity);

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
