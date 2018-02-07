package com.elvaco.mvp.core.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.core.spi.repository.Settings;

class MockSettings implements Settings {

  private final List<Setting> settings;

  MockSettings() {
    this.settings = new ArrayList<>();
  }

  @Override
  public List<Setting> findAll() {
    return settings;
  }

  @Override
  public Optional<Setting> findByName(String name) {
    return settings.stream().filter(s -> s.name.equals(name)).findAny();
  }

  @Override
  public Setting save(Setting setting) {
    settings.add(setting);
    return setting;
  }
}
