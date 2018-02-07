package com.elvaco.mvp.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.entity.setting.SettingEntity;
import com.elvaco.mvp.repository.mappers.DomainEntityMapper;

public class SettingMapper implements DomainEntityMapper<Setting, SettingEntity> {

  public SettingMapper() {
  }

  @Override
  public Setting toDomainModel(SettingEntity entity) {
    return new Setting(entity.id, entity.name, entity.value);
  }

  @Override
  public SettingEntity toEntity(Setting domainModel) {
    SettingEntity settingEntity = new SettingEntity();
    settingEntity.id = domainModel.id;
    settingEntity.name = domainModel.name;
    settingEntity.value = domainModel.value;
    return settingEntity;
  }
}