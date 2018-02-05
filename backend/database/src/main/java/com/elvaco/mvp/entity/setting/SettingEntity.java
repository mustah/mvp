package com.elvaco.mvp.entity.setting;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Access(AccessType.FIELD)
@Entity
@Table(name = "mvp_setting")
public class SettingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true)
  public String name;

  @Column(nullable = false)
  public String value;

  public SettingEntity() {}
}
