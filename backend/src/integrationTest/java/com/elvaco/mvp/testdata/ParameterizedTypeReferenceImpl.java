package com.elvaco.mvp.testdata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ParameterizedTypeReferenceImpl implements ParameterizedType {
  private ParameterizedType delegate;
  private final Type[] types;

  public ParameterizedTypeReferenceImpl(ParameterizedType delegate, Type[] types) {
    this.delegate = delegate;
    this.types = Arrays.copyOf(types, types.length);
  }

  @Override
  public Type[] getActualTypeArguments() {
    return Arrays.copyOf(types, types.length);
  }

  @Override
  public Type getRawType() {
    return delegate.getRawType();
  }

  @Override
  public Type getOwnerType() {
    return delegate.getOwnerType();
  }


}
