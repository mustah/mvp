package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.web.dto.MediumDto;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@RestApi("/api/v1/mediums")
public class MediumController {
  @GetMapping
  public List<MediumDto> allMeterDefinitions() {
    return List.of(
      new MediumDto(UUID.randomUUID(), "Water")
    );
  }
}
