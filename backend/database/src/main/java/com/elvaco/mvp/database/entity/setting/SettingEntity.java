package com.elvaco.mvp.database.entity.setting;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import org.hibernate.envers.Audited;

@Access(AccessType.FIELD)
@Entity
@Table(name = "mvp_setting")
@Audited
public class SettingEntity extends IdentifiableType<UUID> {

  @Id
  public UUID id;

  @Column(nullable = false, unique = true)
  public String name;

  @Column(nullable = false)
  public String value;

  SettingEntity() {}

  public SettingEntity(UUID id, String name, String value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }

  @Override
  public UUID getId() {
    return id;
  }
}
