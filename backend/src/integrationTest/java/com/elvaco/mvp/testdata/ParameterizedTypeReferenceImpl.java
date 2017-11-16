package com.elvaco.mvp.testdata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeReferenceImpl implements ParameterizedType {
  private ParameterizedType delegate;
  private Type[] types;

  public ParameterizedTypeReferenceImpl(ParameterizedType delegate, Type[] types) {
    this.delegate = delegate;
    this.types = types;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return types;
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
