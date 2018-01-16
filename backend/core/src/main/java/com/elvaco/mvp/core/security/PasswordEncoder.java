package com.elvaco.mvp.core.security;

public interface PasswordEncoder {

  String encode(CharSequence rawPassword);
}
