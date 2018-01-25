package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.Setting;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingUseCasesTest {

  SettingUseCases settingUseCases;

  @Before
  public void setUp() {
    MockSettings mockSettings = new MockSettings();
    settingUseCases = new SettingUseCases(mockSettings);
  }

  @Test
  public void isDemoDataLoaded() {
    assertThat(settingUseCases.isDemoDataLoaded()).isFalse();
    settingUseCases.setDemoDataLoaded();
    assertThat(settingUseCases.isDemoDataLoaded()).isTrue();
  }

  @Test
  public void isDemoUsersLoaded() {
    assertThat(settingUseCases.isDemoUsersLoaded()).isFalse();
    settingUseCases.setDemoUsersLoaded();
    assertThat(settingUseCases.isDemoUsersLoaded()).isTrue();
  }

  @Test
  public void save() {
    int settingListPreAddSize = settingUseCases.findAll().size();
    settingUseCases.save(new Setting("foo", "bar"));
    assertThat(settingUseCases.findAll()).hasSize(settingListPreAddSize + 1);
  }
}
