package com.elvaco.mvp.core.exception;

public class UpstreamServiceUnavailable extends RuntimeException {

  private static final long serialVersionUID = -807230270337368020L;

  public UpstreamServiceUnavailable(String message) {
    super(message);
  }
}
