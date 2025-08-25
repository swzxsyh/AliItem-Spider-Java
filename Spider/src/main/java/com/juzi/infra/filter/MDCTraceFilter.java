package com.juzi.infra.filter;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class MDCTraceFilter implements Filter {

  final String TRACE_ID = "traceId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      init((HttpServletRequest) request, (HttpServletResponse) response);
      chain.doFilter(request, response);
    } finally {
      MDC.remove(TRACE_ID);
    }
  }

  private void init(HttpServletRequest request, HttpServletResponse response) {
    String traceId = request.getParameter(TRACE_ID);
    traceId = StringUtils.isBlank(traceId) ? getTraceId() : traceId;
    if (Objects.nonNull(response)) {
      response.addHeader(TRACE_ID, traceId);
    }
    MDC.put(TRACE_ID, traceId);
  }

  private String getTraceId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
