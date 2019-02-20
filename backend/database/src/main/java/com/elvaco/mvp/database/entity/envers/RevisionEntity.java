package com.elvaco.mvp.database.entity.envers;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.database.envers.RevisionEntityListener;

import lombok.EqualsAndHashCode;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@org.hibernate.envers.RevisionEntity(value = RevisionEntityListener.class)
@Access(AccessType.FIELD)
@EqualsAndHashCode
@Table(schema = "evoaudit")
public class RevisionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @RevisionNumber
  public long id;

  @RevisionTimestamp
  public long timestamp;

  public UUID userEntityId;
}
