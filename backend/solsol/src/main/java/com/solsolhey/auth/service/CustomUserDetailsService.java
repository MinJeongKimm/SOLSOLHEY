package com.solsolhey.auth.service;

import com.solsolhey.auth.dto.response.CustomUserDetails;
import com.solsolhey.common.exception.EntityNotFoundException;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * principal(이메일)로 UserDetails 로드
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Spring Security의 DaoAuthenticationProvider는 전달된 principal을 여기로 넘깁니다.
        // 우리 앱에선 principal을 이메일로 사용합니다.
        log.debug("Loading user by email(principal): {}", username);
        
        User user = userRepository.findByEmailAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + username));
        
        return new CustomUserDetails(user);
    }

    /**
     * 사용자 ID로 UserDetails 로드 (JWT 토큰용)
     */
    public UserDetails loadUserByUserId(Long userId) {
        log.debug("Loading user by userId: {}", userId);
        
        User user = userRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", userId));
        
        return new CustomUserDetails(user);
    }

    /**
     * 이메일로 UserDetails 로드
     */
    public UserDetails loadUserByEmail(String email) {
        log.debug("Loading user by email: {}", email);
        
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + email));
        
        return new CustomUserDetails(user);
    }
}
