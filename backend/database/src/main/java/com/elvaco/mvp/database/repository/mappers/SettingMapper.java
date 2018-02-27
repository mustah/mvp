package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.database.entity.setting.SettingEntity;

public class SettingMapper implements DomainEntityMapper<Setting, SettingEntity> {

  @Override
  public Setting toDomainModel(SettingEntity entity) {
    return new Setting(entity.id, entity.name, entity.value);
  }

  @Override
  public SettingEntity toEntity(Setting domainModel) {
    return new SettingEntity(domainModel.id, domainModel.name, domainModel.value);
  }
}
