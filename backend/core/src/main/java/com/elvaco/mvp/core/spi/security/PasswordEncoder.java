package com.elvaco.mvp.core.spi.security;

public interface PasswordEncoder {

  String encode(CharSequence rawPassword);
}
