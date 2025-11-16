package com.elgris.usersapi.configuration;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * Configuration for Prometheus metrics export
 * Compatible with Spring Boot 1.5.x
 */
@Configuration
public class PrometheusConfiguration {

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        DefaultExports.initialize();
        return new ServletRegistrationBean(new MetricsServlet(), "/actuator/prometheus");
    }

    @Bean
    public SpringBootMetricsCollector springBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {
        SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector(publicMetrics);
        springBootMetricsCollector.register();
        return springBootMetricsCollector;
    }
}
