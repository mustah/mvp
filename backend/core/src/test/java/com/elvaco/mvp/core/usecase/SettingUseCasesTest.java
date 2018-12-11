package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.Setting;
import com.elvaco.mvp.testing.repository.MockSettings;

import org.junit.Before;
import org.junit.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SettingUseCasesTest {

  private SettingUseCases settingUseCases;

  @Before
  public void setUp() {
    settingUseCases = new SettingUseCases(new MockSettings());
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
    settingUseCases.save(new Setting(randomUUID(), "foo", "bar"));
    assertThat(settingUseCases.findAll()).hasSize(settingListPreAddSize + 1);
  }
}
