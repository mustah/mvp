package com.elvaco.mvp.testing.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.Identifiable;

abstract class MockRepository<K, V extends Identifiable<K>> {

  private final Map<K, V> repository = new HashMap<>();

  protected abstract V copyWithId(K id, V entity);

  protected abstract K generateId();

  final V saveMock(V entity) {
    if (entity.getId() != null) {
      return repository.put(entity.getId(), entity);
    } else {
      K id = generateId();
      V withId = copyWithId(id, entity);
      repository.put(id, withId);
      return withId;
    }
  }

  final Stream<V> filter(Predicate<V> predicate) {
    return allMocks().stream().filter(predicate);
  }

  final List<V> allMocks() {
    return new ArrayList<>(repository.values());
  }

  final long nextId() {
    return (long) repository.size() + 1;
  }
}
