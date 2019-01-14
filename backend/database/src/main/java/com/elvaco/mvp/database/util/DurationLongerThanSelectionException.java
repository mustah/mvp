package com.elvaco.mvp.database.util;

public class DurationLongerThanSelectionException extends IllegalArgumentException {
  public DurationLongerThanSelectionException() {
    super("Threshold duration too long to fit in selection period");
  }
}
