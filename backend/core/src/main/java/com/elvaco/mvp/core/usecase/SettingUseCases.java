package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.core.spi.repository.Settings;

import static java.util.UUID.randomUUID;

public class SettingUseCases {

  private static final String DEMO_DATA_LOADED = "Demo data loaded";
  private static final String DEMO_USERS_LOADED = "Demo users loaded";
  private static final String DEMO_MEASUREMENTS_LOADED = "Demo measurements loaded";
  private static final String TRUE = "true";
  private static final String FALSE = "false";

  private final Settings settings;

  public SettingUseCases(Settings settings) {
    this.settings = settings;
  }

  public boolean isDemoDataLoaded() {
    return isSettingTrue(DEMO_DATA_LOADED);
  }

  public boolean isDemoUsersLoaded() {
    return isSettingTrue(DEMO_USERS_LOADED);
  }

  public boolean isDemoMeasurementsLoaded() {
    return isSettingTrue(DEMO_MEASUREMENTS_LOADED);
  }

  public void setDemoDataLoaded() {
    setSettingTrue(DEMO_DATA_LOADED);
  }

  public void setDemoUsersLoaded() {
    setSettingTrue(DEMO_USERS_LOADED);
  }

  public void setDemoMeasurementsLoaded() {
    setSettingTrue(DEMO_MEASUREMENTS_LOADED);
  }

  public Setting save(Setting setting) {
    return settings.save(setting);
  }

  public List<Setting> findAll() {
    return settings.findAll();
  }

  private boolean isSettingTrue(String setting) {
    return valueOf(setting).orElse(FALSE).equalsIgnoreCase(TRUE);
  }

  private void setSettingTrue(String setting) {
    save(new Setting(randomUUID(), setting, TRUE));
  }

  private Optional<String> valueOf(String name) {
    return settings.findByName(name).map(setting -> setting.value);
  }
}
