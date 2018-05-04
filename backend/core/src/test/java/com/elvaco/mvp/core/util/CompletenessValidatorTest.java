package com.elvaco.mvp.core.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CompletenessValidatorTest {

  @Test
  public void allTruePredicates() {
    Object o = new Object();
    CompletenessValidator<Object> completenessValidator = new CompletenessValidator<>(
      object -> true,
      object -> true
    );
    assertThat(completenessValidator.isComplete(o)).isTrue();
  }

  @Test
  public void multipleFalsePredicates() {
    Object o = new Object();
    CompletenessValidator<Object> completenessValidator = new CompletenessValidator<>(
      object -> false,
      object -> false
    );
    assertThat(completenessValidator.isComplete(o)).isFalse();
  }

  @Test
  public void mixedResults() {
    Object o = new Object();
    CompletenessValidator<Object> completenessValidator = new CompletenessValidator<>(
      object -> false,
      object -> true
    );
    assertThat(completenessValidator.isComplete(o)).isFalse();
  }

  @Test
  public void nullObject() {
    CompletenessValidator<Object> completenessValidator = new CompletenessValidator<>(
      object -> object.toString().equals("something"),
      object -> true
    );
    assertThatThrownBy(() -> completenessValidator.isComplete(null))
      .hasMessageContaining("Can not test completeness for null object");
  }

  @Test
  public void negated() {
    CompletenessValidator<Object> objectCompletenessValidator = new CompletenessValidator<>(
      object -> true,
      object -> false
    );
    assertThat(objectCompletenessValidator.isIncomplete(new Object())).isTrue();

  }
}
