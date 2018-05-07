package com.elvaco.mvp.configuration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static java.util.Collections.emptyList;

@EnableSwagger2
@Configuration
class SwaggerConfig {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
      .select()
      .apis(RequestHandlerSelectors.any())
      .paths(PathSelectors.ant(WebSecurityConfig.apiPath() + "/**"))
      .build()
      .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
      "evo REST API",
      "REST API for integrating with evo",
      "1.0",
      "tos-url",
      new Contact(
        "Elvaco Support",
        "https://www.elvaco.se/en/content/contact",
        "support@elvaco.se"
      ),
      "license",
      "license-url",
      emptyList()
    );
  }
}
