package com.elvaco.mvp.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SubOrganisationRequestDto {

  public String name;

  public String slug;

  public UUID selectionId;
}
