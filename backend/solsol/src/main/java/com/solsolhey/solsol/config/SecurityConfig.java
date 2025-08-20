package com.solsolhey.solsol.config;

import com.solsolhey.solsol.auth.jwt.JwtAuthenticationFilter;
import com.solsolhey.solsol.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
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

import java.util.Arrays;
import java.util.List;

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
                authz.requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/health").permitAll();
                
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
        
        // 허용할 Origin (환경변수에서 가져오거나 기본값 사용)
        String allowedOrigins = System.getProperty("CORS_ALLOWED_ORIGINS", 
                System.getenv().getOrDefault("CORS_ALLOWED_ORIGINS", 
                        "http://localhost:3000,http://localhost:5173,http://localhost:8080"));
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));
        
        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // 브라우저 캐시 시간 (초)
        configuration.setMaxAge(3600L);

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
