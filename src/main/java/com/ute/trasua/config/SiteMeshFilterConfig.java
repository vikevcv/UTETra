package com.ute.trasua.config;

import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SiteMesh 3 Filter — TẠM THỜI TẮT để debug white page.
 * Sau khi fix xong, bỏ comment @Bean để bật lại.
 */
@Configuration
public class SiteMeshFilterConfig {

    @Bean
    public FilterRegistrationBean<ConfigurableSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<ConfigurableSiteMeshFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new ConfigurableSiteMeshFilter());
        filter.addUrlPatterns("/*");
        filter.setOrder(1);
        filter.setAsyncSupported(true);
        return filter;
    }
}
