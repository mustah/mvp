package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.database.entity.setting.SettingEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SettingEntityMapper {

  public static Setting toDomainModel(SettingEntity entity) {
    return new Setting(entity.id, entity.name, entity.value);
  }

  public static SettingEntity toEntity(Setting domainModel) {
    return new SettingEntity(domainModel.getId(), domainModel.name, domainModel.value);
  }
}
