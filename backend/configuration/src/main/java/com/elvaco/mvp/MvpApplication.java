package com.elvaco.mvp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import static java.util.Collections.singletonList;

@EnableZuulProxy
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = "com.elvaco.mvp")
@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
public class MvpApplication extends WebMvcConfigurerAdapter {

  public static void main(String[] args) {
    SpringApplication.run(MvpApplication.class, args);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
      .addResourceLocations("classpath:/static/")
      .resourceChain(false)
      .addResolver(new PushStateResourceResolver());
  }

  /**
   * Produces a {@link LocaleResolver} bean, used for resolving the current locale.
   *
   * @return The {@link LocaleResolver} bean
   */
  @Bean
  public LocaleResolver localeResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    Locale sweLocale = new Locale.Builder().setLanguage("sv").setRegion("SE").build();
    localeResolver.setDefaultLocale(sweLocale);
    return localeResolver;
  }

  /**
   * Produces a {@link LocaleChangeInterceptor} bean, used for intercepting locale changes.
   *
   * @return The {@link LocaleChangeInterceptor} bean
   */
  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
    lci.setParamName("lang");
    return lci;
  }

  /**
   * Produces a {@link MessageSource} bean, used for resolving messages for i18n.
   *
   * @return The {@link MessageSource} bean
   */
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("i18n/messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
      .addMapping("/**")
      .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
      .maxAge(3600);
  }

  private static class PushStateResourceResolver implements ResourceResolver {
    private final Resource index = new ClassPathResource("/static/index.html");
    private final List<String> handledExtensions = Arrays.asList(
      "html",
      "js",
      "json",
      "csv",
      "css",
      "png",
      "svg",
      "eot",
      "ttf",
      "woff",
      "appcache",
      "jpg",
      "jpeg",
      "gif",
      "ico"
    );
    private final List<String> ignoredPaths = singletonList("api");

    @Override
    public Resource resolveResource(
      HttpServletRequest request, String requestPath, List<?
      extends Resource> locations, ResourceResolverChain chain
    ) {
      return resolve(requestPath, locations);
    }

    @Override
    public String resolveUrlPath(
      String resourcePath, List<? extends Resource> locations,
      ResourceResolverChain chain
    ) {
      Resource resolvedResource = resolve(resourcePath, locations);
      if (resolvedResource == null) {
        return null;
      }
      try {
        return resolvedResource.getURL().toString();
      } catch (IOException e) {
        return resolvedResource.getFilename();
      }
    }

    private Resource resolve(String requestPath, List<? extends Resource> locations) {
      if (isIgnored(requestPath)) {
        return null;
      }
      if (isHandled(requestPath)) {
        return locations.stream()
          .map(loc -> createRelative(loc, requestPath))
          .filter(resource -> resource != null && resource.exists())
          .findFirst()
          .orElse(null);
      }
      return index;
    }

    private Resource createRelative(Resource resource, String relativePath) {
      try {
        return resource.createRelative(relativePath);
      } catch (IOException e) {
        return null;
      }
    }

    private boolean isIgnored(String path) {
      return ignoredPaths.contains(path);
    }

    private boolean isHandled(String path) {
      String extension = StringUtils.getFilenameExtension(path);
      return handledExtensions.stream().anyMatch(ext -> ext.equals(extension));
    }
  }
}
