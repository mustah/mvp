package com.elvaco.mvp.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserSelectionDto {
  public UUID id;
  public UUID ownerUserId;
  public String name;
  public String data;
  public UUID organisationId;
}
