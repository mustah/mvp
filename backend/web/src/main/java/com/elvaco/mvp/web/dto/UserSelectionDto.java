package com.elvaco.mvp.web.dto;

import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserSelectionDto {
  public UUID id;
  public UUID ownerUserId;
  public String name;
  public ObjectNode selectionParameters;
  public UUID organisationId;
}
