package com.elvaco.mvp.core.domainmodels;

@FunctionalInterface
public interface Identifiable<T> {

  T getId();
}
