package com.ts.messenger.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ts.messenger.multi.tenant.RequestContext;

/**
 * Container threads are pooled: always drop the tenant bound by the previous request so a request
 * can never run against another customer database.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		RequestContext.clear();
		try {
			filterChain.doFilter(request, response);
		} finally {
			RequestContext.clear();
		}
	}
}
