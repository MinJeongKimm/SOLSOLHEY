package com.solsolhey.auth.dto.response;

import com.solsolhey.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails 구현체
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 단순히 ROLE_USER만 부여
        // 추후 역할 관리가 필요하면 User 엔티티에 roles 필드를 추가
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 사용자 ID 반환
     */
    public Long getUserId() {
        return user.getUserId();
    }

    /**
     * 닉네임 반환
     */
    public String getNickname() {
        return user.getNickname();
    }

    /**
     * 이메일 반환
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 총 포인트 반환
     */
    public Integer getTotalPoints() {
        return user.getTotalPoints();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 기능 미사용
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 기능 미사용
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격증명 만료 기능 미사용
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive(); // User 엔티티의 활성화 상태
    }
}
