package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Access(AccessType.FIELD)
@Table(name = "display_quantity")
@Audited
public class DisplayQuantityEntity extends IdentifiableType<DisplayQuantityPk> {

  private static final long serialVersionUID = 1470422578344415571L;

  @EmbeddedId
  public DisplayQuantityPk pk;

  public String displayUnit;

  public int decimals;

  @Override
  public DisplayQuantityPk getId() {
    return pk;
  }
}
