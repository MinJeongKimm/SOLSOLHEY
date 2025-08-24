package com.solsolhey.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Ensures CookieCsrfTokenRepository actually emits/maintains the XSRF-TOKEN cookie
 * by touching the CsrfToken on every request.
 */
public class CsrfCookieSeedFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
        if (token == null) token = (CsrfToken) req.getAttribute("_csrf");
        chain.doFilter(req, res);
    }
}
