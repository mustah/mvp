package com.elvaco.mvp.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnauthorizedDto {

  @Builder.Default
  public Integer status = HttpStatus.UNAUTHORIZED.value();
  @Builder.Default
  public String error = HttpStatus.UNAUTHORIZED.getReasonPhrase();
  public String message;
  public String path;
}
