package com.elvaco.mvp.web.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@RestApi("/api/v1/errors")
class ErrorController {

  @GetMapping
  public void error() {
    throw new RuntimeException("error");
  }
}
