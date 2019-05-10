package com.elvaco.mvp.web.converter;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.AssetType;

import org.springframework.core.convert.converter.Converter;

public class AssetTypeConverter implements Converter<String, Optional<AssetType>> {

  @Override
  public Optional<AssetType> convert(String source) {
    return AssetType.fromString(source);
  }
}
