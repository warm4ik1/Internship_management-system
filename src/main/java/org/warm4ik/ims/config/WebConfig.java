package org.warm4ik.ims.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${file.storage.url-prefix}")
  private String urlPrefix;

  @Value("${file.storage.dir}")
  private String storageDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {

    String location = Paths.get(storageDir).toAbsolutePath().toUri().toString();

    registry.addResourceHandler(urlPrefix + "**").addResourceLocations(location);

    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/img");
  }

  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());

    supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
    converter.setSupportedMediaTypes(supportedMediaTypes);
    return converter;
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/auth").setViewName("forward:/auth/index.html");
  }
}
