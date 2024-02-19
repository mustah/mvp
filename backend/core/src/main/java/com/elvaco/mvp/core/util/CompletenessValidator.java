package com.elvaco.mvp.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CompletenessValidator<T> {

  private final List<Predicate<T>> predicates;

  @SafeVarargs
  CompletenessValidator(Predicate<T>... predicates) {
    this(List.of(predicates));
  }

  private CompletenessValidator(List<Predicate<T>> predicates) {
    this.predicates = predicates;
  }

  public boolean isComplete(T thing) {
    Objects.requireNonNull(thing, "Can not test completeness for null object");
    return predicates.stream().allMatch(predicate -> predicate.test(thing));
  }

  public boolean isIncomplete(T thing) {
    return !isComplete(thing);
  }
}
