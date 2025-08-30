package com.solsolhey.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.solsolhey.auth.jwt.JwtAuthenticationFilter;
import com.solsolhey.auth.service.CustomUserDetailsService;
import com.solsolhey.common.security.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import com.solsolhey.common.security.CsrfCookieSeedFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solsolhey.auth.jwt.JwtTokenProvider;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final RateLimitingFilter rateLimitingFilter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, org.springframework.beans.factory.ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider) throws Exception {
        log.debug("=== SecurityConfig building filter chain ===");

        // Optional SPA-friendly handler; prevents relying on request attribute name.
        var csrfAttrHandler = new CsrfTokenRequestAttributeHandler();
        csrfAttrHandler.setCsrfRequestAttributeName(null);

        // Configure CSRF cookie attributes for cross-origin SPA
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepo.setCookieCustomizer((builder) -> builder
            .secure(true)
            .sameSite("None")
        );

        http
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepo)
                .csrfTokenRequestHandler(csrfAttrHandler)
                // ★ Make absolutely sure auth endpoints bypass CSRF
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/api/v1/auth/**"),
                    new AntPathRequestMatcher("/h2-console/**")
                )
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/api/v1/finance/**"),
                    // 임시: 교차 출처 CSRF 토큰 동기화 이슈 우회 (보안 강화 시 제거 권장)
                    new AntPathRequestMatcher("/api/v1/mascot/**"),
                    new AntPathRequestMatcher("/api/v1/friends/**"),
                    new AntPathRequestMatcher("/api/ranking/**")
                )

            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/actuator/**", "/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/mascot/view/public").permitAll() // 공개 마스코트 조회는 인증 불필요
                .requestMatchers("/api/v1/shop/items/public").permitAll() // 공개 상품 목록 조회는 인증 불필요
                .requestMatchers("/api/v1/mascot/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/friends/requests").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/attendance").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/attendance/**").authenticated()
                .anyRequest().permitAll()
            )
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // Run your custom rate limit filter early (kept as before)
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

        // JWT before UsernamePasswordAuthenticationFilter (only if present)
        JwtAuthenticationFilter jwtFilter = jwtFilterProvider.getIfAvailable();
        if (jwtFilter != null) {
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }

        // ★ Ensure XSRF-TOKEN cookie is always issued/maintained
        http.addFilterAfter(new CsrfCookieSeedFilter(), CsrfFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String origins = environment.getProperty("cors.allowed-origins", "http://localhost:5173");
        String methods = environment.getProperty("cors.allowed-methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        String headers = environment.getProperty("cors.allowed-headers", "*");
        boolean allowCreds = Boolean.parseBoolean(environment.getProperty("cors.allow-credentials", "true"));
        long maxAge = Long.parseLong(environment.getProperty("cors.max-age", "3600"));

        configuration.setAllowedOriginPatterns(Arrays.stream(origins.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(Arrays.stream(methods.split(",")).map(String::trim).toList());
        configuration.setAllowedHeaders(Arrays.stream(headers.split(",")).map(String::trim).toList());
        configuration.setAllowCredentials(allowCreds);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }



    /**
     * 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 관리자
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * RateLimitingFilter 자동 등록 비활성화
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(RateLimitingFilter filter) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * JWT 인증 필터 빈 등록
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService,
            ObjectMapper objectMapper,
            Environment environment) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, objectMapper, environment);
    }

    /**
     * JwtAuthenticationFilter 자동 등록 비활성화
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setEnabled(false);
        return registration;
    }

    // Removed custom AuthenticationProvider bean to let Spring Boot auto-configure
    // DaoAuthenticationProvider from UserDetailsService + PasswordEncoder, which
    // avoids the InitializeUserDetailsManagerConfigurer warning.

    /**
     * 개발환경 여부 확인
     * local, dev 프로파일인 경우 개발환경으로 판단
     */
    private boolean isDevelopmentEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        
        // 활성 프로파일이 없을 때는 기본값 확인
        if (activeProfiles.length == 0) {
            String[] defaultProfiles = environment.getDefaultProfiles();
            activeProfiles = defaultProfiles.length > 0 ? defaultProfiles : new String[]{"default"};
        }
        
        for (String profile : activeProfiles) {
            if ("local".equals(profile) || "dev".equals(profile) || "default".equals(profile)) {
                return true;
            }
        }
        
        return false;
    }
}
