package com.elvaco.mvp.database.entity.setting;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@NoArgsConstructor
@AllArgsConstructor
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

  @Override
  public UUID getId() {
    return id;
  }
}
