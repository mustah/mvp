package com.elvaco.mvp.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@RestApi
@Slf4j
@Profile("json-server-proxy")
public class JsonServerProxyController {
  private static final int JSON_SERVER_PORT = 3000;

  @RequestMapping("**")
  public ModelAndView proxy(HttpServletRequest httpServletRequest,
                            HttpServletResponse httpServletResponse) {
    String requestedUri = httpServletRequest.getRequestURI();
    String proxyUri = String.format(
      "http://%s:%d/%s",
      httpServletRequest.getServerName(),
      JSON_SERVER_PORT,
      requestedUri.substring("/api/".length(), requestedUri.length())
    );

    log.info(String.format("Redirecting request for URI %s to %s!", requestedUri, proxyUri));
    return new ModelAndView("redirect:" + proxyUri);
  }
}
