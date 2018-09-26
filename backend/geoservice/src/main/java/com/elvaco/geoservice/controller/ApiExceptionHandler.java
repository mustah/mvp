package com.elvaco.geoservice.controller;

import com.elvaco.geoservice.dto.FieldErrorsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static java.util.stream.Collectors.toList;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(BindException.class)
  public ResponseEntity<FieldErrorsDto> handle(BindException exception) {
    return ResponseEntity.badRequest()
      .body(new FieldErrorsDto(
        HttpStatus.BAD_REQUEST.value(),
        exception.getFieldErrors().stream()
          .map(DefaultMessageSourceResolvable::getDefaultMessage)
          .collect(toList())
      ));
  }
}
