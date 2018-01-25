package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Setting;

public interface Settings {
  List<Setting> findAll();

  Optional<Setting> findByName(String name);

  Setting save(Setting setting);
}
