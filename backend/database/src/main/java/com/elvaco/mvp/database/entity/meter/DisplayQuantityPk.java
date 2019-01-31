package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.elvaco.mvp.core.domainmodels.DisplayMode;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Embeddable
public class DisplayQuantityPk implements Serializable {

  private static final long serialVersionUID = 9023894151522851555L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "quantity_id", nullable = false)
  public QuantityEntity quantity;

  @Column(name = "meter_definition_id", nullable = false)
  public Integer meterDefinitionId;

  @Enumerated(EnumType.ORDINAL)
  public DisplayMode displayMode;
}
