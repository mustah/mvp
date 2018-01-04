package com.elvaco.mvp.bootstrap;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Proxies requests to unfinished/non-existent endpoints (for some value of unfinished) to an
 * instance of json-server where that endpoint is implemented.
 * For development use only!
 */
@Slf4j
@Component
@Profile("json-server-proxy")
public class JsonServerProxyRunner implements CommandLineRunner {

  private final ResourceLoader resourceLoader;

  @Autowired
  public JsonServerProxyRunner(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void run(String... args) throws Exception {
    if (shouldStartJsonServer()) {
      new ProcessBuilder(getNodePath(), getJsonServerPath(), getDbJsonPath())
        .inheritIO()
        .start();
    }
  }

  private String getResourceFilename(String resourcePath) throws IOException {
    Resource resource = resourceLoader.getResource(resourcePath);
    if (resource == null) {
      throw new IOException("Could not find resource: " + resourcePath + "!");
    }
    return resource.getFile().getAbsolutePath();
  }

  private String getPropertyOrResourcePath(String propertyName, String resourcePath)
    throws IOException {
    String property = System.getProperty(propertyName);
    if (property != null) {
      return property;
    }
    return getResourceFilename(resourcePath);
  }

  private String getJsonServerPath() throws IOException {
    return getPropertyOrResourcePath("json-server-proxy.path", "classpath:json-server");
  }

  private String getDbJsonPath() throws IOException {
    return getPropertyOrResourcePath("json-server-proxy.db", "classpath:db.json");
  }

  private String getNodePath() {
    return System.getProperty(
      "json-server-proxy.nodejs.path",
      "node" // fallback to looking on $PATH if property is not set
    );
  }

  private boolean shouldStartJsonServer() {
    return !Boolean.parseBoolean(System.getProperty("json-server-proxy.proxy-only"));
  }
}
