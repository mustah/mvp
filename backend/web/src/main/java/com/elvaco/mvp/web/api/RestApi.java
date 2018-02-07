package com.elvaco.mvp.web.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.elvaco.mvp.web.util.Constants.API_V1;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping
@interface RestApi {
  @AliasFor(attribute = "path", annotation = RequestMapping.class)
  String value() default API_V1;
}
