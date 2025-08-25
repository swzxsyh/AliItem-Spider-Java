package com.juzi.infra.config;

import com.juzi.infra.filter.MDCTraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class CommonConfiguration {

  @Bean
  public FilterRegistrationBean<MDCTraceFilter> registerCustomFilter() {
    FilterRegistrationBean<MDCTraceFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new MDCTraceFilter());
    registrationBean.setName("MDCTraceFilter");
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registrationBean.setEnabled(Boolean.TRUE);
    registrationBean.addUrlPatterns("/*");
    return registrationBean;
  }
}
