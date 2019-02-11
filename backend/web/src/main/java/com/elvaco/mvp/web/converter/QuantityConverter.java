package com.elvaco.mvp.web.converter;

import com.elvaco.mvp.core.domainmodels.QuantityParameter;

import org.springframework.core.convert.converter.Converter;

public class QuantityConverter implements Converter<String, QuantityParameter> {

  @Override
  public QuantityParameter convert(String source) {
    return QuantityParameter.of(source);
  }
}
