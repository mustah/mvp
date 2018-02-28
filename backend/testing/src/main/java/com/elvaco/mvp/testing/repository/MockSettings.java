package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.core.spi.repository.Settings;

public class MockSettings extends MockRepository<Setting> implements Settings {

  @Override
  protected Optional<Long> getId(Setting entity) {
    return Optional.ofNullable(entity.id);
  }

  @Override
  protected Setting copyWithId(Long id, Setting entity) {
    return new Setting(id, entity.name, entity.value);
  }

  @Override
  public List<Setting> findAll() {
    return allMocks();
  }

  @Override
  public Optional<Setting> findByName(String name) {
    return filter(s -> s.name.equals(name)).findAny();
  }

  @Override
  public Setting save(Setting setting) {
    return saveMock(setting);
  }
}
