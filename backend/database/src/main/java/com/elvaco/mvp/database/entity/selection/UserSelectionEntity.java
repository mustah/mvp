package com.elvaco.mvp.database.entity.selection;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.EntityType;
import com.elvaco.mvp.database.entity.meter.JsonField;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_selection")
@Access(AccessType.FIELD)
public class UserSelectionEntity extends EntityType<UUID> {

  private static final long serialVersionUID = -64835523791321552L;

  @Id
  public UUID id;

  @Column(nullable = false)
  public UUID ownerUserId;

  @Column(nullable = false)
  public String name;

  @Column(nullable = false)
  public JsonField selectionParameters;

  @Column(nullable = false)
  public UUID organisationId;

  @Override
  public UUID getId() {
    return id;
  }
}
