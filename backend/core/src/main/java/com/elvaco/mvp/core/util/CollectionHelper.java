package com.elvaco.mvp.core.util;

import java.util.Collection;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionHelper {

  public static <E> boolean isNotEmpty(Collection<E> collection) {
    return !collection.isEmpty();
  }
}
