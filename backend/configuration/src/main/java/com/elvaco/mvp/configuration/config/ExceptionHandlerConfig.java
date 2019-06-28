package com.elvaco.mvp.configuration.config;

import java.util.Map;
import javax.servlet.RequestDispatcher;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

@Configuration
class ExceptionHandlerConfig {

  private static final String GENERAL_ERROR_MESSAGE = "An error occured, please contact support";

  @Bean
  ErrorAttributes errorAttributes() {
    return new DefaultErrorAttributes() {
      @Override
      public Map<String, Object> getErrorAttributes(
        WebRequest webRequest,
        boolean includeStackTrace
      ) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(
          webRequest,
          includeStackTrace
        );
        Object errorMessage = webRequest.getAttribute(
          RequestDispatcher.ERROR_MESSAGE,
          RequestAttributes.SCOPE_REQUEST
        );

        Object status = webRequest.getAttribute(
          RequestDispatcher.ERROR_STATUS_CODE,
          RequestAttributes.SCOPE_REQUEST
        );

        if (errorMessage != null && status.equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
          errorAttributes.put("message", GENERAL_ERROR_MESSAGE);
        }
        return errorAttributes;
      }
    };
  }
}
