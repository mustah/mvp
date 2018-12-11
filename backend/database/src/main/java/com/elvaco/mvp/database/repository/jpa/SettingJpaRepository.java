package com.elvaco.mvp.database.repository.jpa;

import java.util.Optional;

import com.elvaco.mvp.database.entity.setting.SettingEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingJpaRepository extends JpaRepository<SettingEntity, Long> {
  Optional<SettingEntity> findByName(String name);
}
