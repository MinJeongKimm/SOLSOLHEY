package com.solsolhey.solsol.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.solsolhey.auth.jwt.JwtAuthenticationFilter;
import com.solsolhey.auth.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용으로 인해)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 비활성화 (JWT 사용으로 인해)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 요청별 권한 설정
            .authorizeHttpRequests(authz -> {
                // 공개 API (인증 불필요)
                authz.requestMatchers("/api/v1/auth/**").permitAll()  // 실제 API 경로에 맞게 수정
                    .requestMatchers("/auth/**").permitAll()  // 혹시 모를 레거시 경로
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/health").permitAll()
                    .requestMatchers("/api/v1/shop/**").permitAll();  // Shop API 허용
                
                // 개발환경에서만 허용되는 엔드포인트
                if (isDevelopmentEnvironment()) {
                    authz.requestMatchers("/h2-console/**").permitAll()  // H2 Console
                         .requestMatchers("/swagger-ui/**").permitAll()   // Swagger UI
                         .requestMatchers("/v3/api-docs/**").permitAll(); // Swagger API Docs
                }
                
                // 기타 모든 요청은 인증 필요
                authz.anyRequest().authenticated();
            })
            
            // JWT 인증 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 인증 제공자 설정
            .authenticationProvider(authenticationProvider())
            
            // H2 Console을 위한 Frame Options 설정
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 직접 환경변수에서 CORS 설정 읽어오기
        String allowedOrigins = environment.getProperty("CORS_ALLOWED_ORIGINS", "http://localhost:3000,http://localhost:5173");
        String allowedMethods = environment.getProperty("CORS_ALLOWED_METHODS", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        String allowedHeaders = environment.getProperty("CORS_ALLOWED_HEADERS", "*");
        boolean allowCredentials = Boolean.parseBoolean(environment.getProperty("CORS_ALLOW_CREDENTIALS", "true"));
        long maxAge = Long.parseLong(environment.getProperty("CORS_MAX_AGE", "3600"));
        
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
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
     * 인증 제공자
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

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
