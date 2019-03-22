package com.elvaco.mvp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;

import com.elvaco.mvp.configuration.config.ApmInterceptor;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.web.converter.CityConverter;
import com.elvaco.mvp.web.converter.QuantityConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import static java.util.Collections.singletonList;

@EnableWebSecurity
@SpringBootApplication(
  scanBasePackages = "com.elvaco.mvp",
  exclude = RepositoryRestMvcAutoConfiguration.class
)
@EnableScheduling
public class MvpApplication implements WebMvcConfigurer {

  private final AuthenticatedUser currentUser;

  @Autowired
  public MvpApplication(AuthenticatedUser currentUser) {
    this.currentUser = currentUser;
  }

  public static void main(String[] args) {
    SpringApplication.run(MvpApplication.class, args);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new CityConverter());
    registry.addConverter(new QuantityConverter());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ApmInterceptor(currentUser));
    registry.addInterceptor(localeChangeInterceptor());
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
      .addResourceLocations("classpath:/static/")
      .setCacheControl(
        CacheControl.maxAge(0, TimeUnit.DAYS)
          .cachePrivate()
          .mustRevalidate())
      .resourceChain(true)
      .addResolver(new PushStateResourceResolver());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
      .addMapping("/**")
      .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
      .maxAge(3600);
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
