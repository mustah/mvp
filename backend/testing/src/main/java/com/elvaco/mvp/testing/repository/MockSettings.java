package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.core.spi.repository.Settings;

import static java.util.UUID.randomUUID;

public class MockSettings extends MockRepository<UUID, Setting> implements Settings {

  @Override
  protected Setting copyWithId(UUID id, Setting entity) {
    return new Setting(id, entity.name, entity.value);
  }

  @Override
  protected UUID generateId() {
    return randomUUID();
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
