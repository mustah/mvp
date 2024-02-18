package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.core.spi.repository.Settings;
import com.elvaco.mvp.database.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.database.repository.mappers.SettingEntityMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SettingRepository implements Settings {

  private final SettingJpaRepository settingJpaRepository;

  @Override
  public List<Setting> findAll() {
    return settingJpaRepository.findAll().stream()
      .map(SettingEntityMapper::toDomainModel)
      .toList();
  }

  @Override
  public Optional<Setting> findByName(String name) {
    return settingJpaRepository.findByName(name)
      .map(SettingEntityMapper::toDomainModel);
  }

  @Override
  public Setting save(Setting setting) {
    return SettingEntityMapper.toDomainModel(
      settingJpaRepository.save(SettingEntityMapper.toEntity(setting))
    );
  }
}
