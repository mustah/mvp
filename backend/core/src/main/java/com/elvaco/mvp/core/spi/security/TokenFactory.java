package com.elvaco.mvp.core.spi.security;

@FunctionalInterface
public interface TokenFactory {

  String newToken();
}
