package com.elvaco.mvp.api;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.elvaco.mvp.dto.ErrorMessageDTO;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  @ExceptionHandler
  @ResponseBody
  public ResponseEntity<ErrorMessageDTO> handle(Exception exception) {
    log.warn("Exception occurred while processing request", exception);
    HttpStatus responseHttpStatus = resolveHttpStatus(exception);
    ErrorMessageDTO dto = new ErrorMessageDTO(exception.getMessage(), responseHttpStatus.value());
    return new ResponseEntity<>(dto, responseHttpStatus);
  }

  private HttpStatus resolveHttpStatus(Exception exception) {
    return Optional.ofNullable(findMergedAnnotation(exception.getClass(), ResponseStatus.class))
      .map(ResponseStatus::value)
      .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
