package com.ute.trasua.config;

import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Đăng ký SiteMesh 3 Filter tùy biến cho Spring Boot,
 * tương thích 100% với Tomcat 11 và Jakarta Servlet 6.0.
 */
@Configuration
public class SiteMeshFilterConfig {

    @Bean
    public FilterRegistrationBean<ConfigurableSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<ConfigurableSiteMeshFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new ConfigurableSiteMeshFilter());
        filter.addUrlPatterns("/*");
        filter.setOrder(1);
        return filter;
    }
}
