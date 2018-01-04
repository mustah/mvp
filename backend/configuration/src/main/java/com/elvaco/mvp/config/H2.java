package com.elvaco.mvp.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Profile;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile("h2")
public @interface H2 {
}
