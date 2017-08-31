package com.elvaco.mvp.api;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.elvaco.mvp.dto.ErrorMessageDTO;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@ControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler
  @ResponseBody
  public ResponseEntity<ErrorMessageDTO> handle(Exception exception) {
    HttpStatus responseHttpStatus = resolveHttpStatus(exception);
    return new ResponseEntity<>(new ErrorMessageDTO(exception.getMessage()), responseHttpStatus);
  }

  private HttpStatus resolveHttpStatus(Exception exception) {
    return Optional.ofNullable(findMergedAnnotation(exception.getClass(), ResponseStatus.class))
      .map(ResponseStatus::value)
      .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
