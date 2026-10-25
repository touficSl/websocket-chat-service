package com.ts.messenger.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Baseline browser hardening headers. The page specific Content-Security-Policy is set by index.jsp.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

	@Value("${messenger.frame-ancestors:'self'}")
	private String frameAncestors;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		response.setHeader("X-Content-Type-Options", "nosniff");
		/* URLs carry the session / room parameters, never leak them to third parties */
		response.setHeader("Referrer-Policy", "no-referrer");
		response.setHeader("Content-Security-Policy", "frame-ancestors " + sanitize(frameAncestors));
		response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
		response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
		if (request.isSecure())
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

		filterChain.doFilter(request, response);
	}

	/* Header injection guard for the configured value */
	private static String sanitize(String value) {
		if (value == null || value.trim().isEmpty())
			return "'self'";
		return value.replaceAll("[\\r\\n;,]", " ").trim();
	}
}
