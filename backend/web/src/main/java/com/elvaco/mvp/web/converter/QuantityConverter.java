package com.elvaco.mvp.web.converter;

import com.elvaco.mvp.core.domainmodels.Quantity;

import org.springframework.core.convert.converter.Converter;

public class QuantityConverter implements Converter<String, Quantity> {

  @Override
  public Quantity convert(String source) {
    return Quantity.of(source);
  }
}
