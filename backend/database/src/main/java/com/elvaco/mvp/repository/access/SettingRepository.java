package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.core.usecase.Settings;
import com.elvaco.mvp.repository.jpa.SettingJpaRepository;

import static java.util.stream.Collectors.toList;

public class SettingRepository implements Settings {
  private final SettingMapper settingMapper;
  private final SettingJpaRepository settingJpaRepository;

  public SettingRepository(SettingJpaRepository settingJpaRepository, SettingMapper settingMapper) {
    this.settingJpaRepository = settingJpaRepository;
    this.settingMapper = settingMapper;
  }

  @Override
  public List<Setting> findAll() {
    return settingJpaRepository.findAll().stream().map(settingMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<Setting> findByName(String name) {
    return settingJpaRepository.findByName(name).map(settingMapper::toDomainModel);
  }

  @Override
  public Setting save(Setting setting) {
    return settingMapper.toDomainModel(
      settingJpaRepository.save(settingMapper.toEntity(setting))
    );
  }
}
