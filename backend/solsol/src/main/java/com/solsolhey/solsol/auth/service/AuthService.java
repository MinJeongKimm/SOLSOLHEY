package com.solsolhey.solsol.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.solsol.auth.dto.LoginRequestDto;
import com.solsolhey.solsol.auth.dto.LoginResponseDto;
import com.solsolhey.solsol.auth.dto.SignUpRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpResponseDto;
import com.solsolhey.solsol.auth.dto.TokenResponseDto;
import com.solsolhey.solsol.auth.jwt.JwtTokenProvider;
import com.solsolhey.solsol.common.exception.AuthException;
import com.solsolhey.solsol.common.exception.BusinessException;
import com.solsolhey.solsol.entity.User;
import com.solsolhey.solsol.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증/인가 관련 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     */
    public SignUpResponseDto signUp(SignUpRequestDto requestDto) {
        log.info("회원가입 시도: username={}, email={}", requestDto.getUsername(), requestDto.getEmail());

        // 중복 검사
        validateDuplicateUser(requestDto.getUsername(), requestDto.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 사용자 생성
        User user = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .passwordHash(encodedPassword)
                .nickname(requestDto.getNickname())
                .campus(requestDto.getCampus())
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        return SignUpResponseDto.from(savedUser);
    }

    /**
     * 로그인
     */
    public LoginResponseDto login(LoginRequestDto requestDto) {
        log.info("로그인 시도: usernameOrEmail={}", requestDto.getUsernameOrEmail());

        try {
            // 사용자 조회
            User user = findUserByUsernameOrEmail(requestDto.getUsernameOrEmail());
            
            // 인증 수행
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            requestDto.getPassword()
                    )
            );

            // 토큰 생성
            TokenResponseDto tokens = generateTokens(user);
            
            log.info("로그인 완료: userId={}, username={}", user.getUserId(), user.getUsername());
            
            return LoginResponseDto.from(user, tokens);

        } catch (AuthenticationException e) {
            log.warn("로그인 실패: usernameOrEmail={}, 원인={}", requestDto.getUsernameOrEmail(), e.getMessage());
            throw new AuthException.InvalidCredentialsException();
        }
    }

    /**
     * 토큰 갱신
     */
    public TokenResponseDto refreshToken(String refreshToken) {
        log.info("토큰 갱신 시도");

        // Refresh Token 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 Refresh Token");
            throw new AuthException.InvalidTokenException("유효하지 않은 Refresh Token입니다.");
        }

        // 사용자 정보 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new AuthException.UserNotFoundException());

        // 새 토큰 생성
        TokenResponseDto tokens = generateTokens(user);
        
        log.info("토큰 갱신 완료: userId={}", userId);
        
        return tokens;
    }

    /**
     * 로그아웃 (클라이언트에서 토큰 삭제)
     * 서버에서는 별도 처리 없음 (토큰 블랙리스트 구현 시 추가 가능)
     */
    public void logout(String accessToken) {
        log.info("로그아웃 처리");
        // 현재는 클라이언트에서 토큰을 삭제하는 것으로 처리
        // 향후 토큰 블랙리스트 기능을 구현할 수 있음
    }

    /**
     * 중복 사용자 검증
     */
    private void validateDuplicateUser(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("이미 사용 중인 사용자명입니다: " + username);
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("이미 사용 중인 이메일입니다: " + email);
        }
    }

    /**
     * 사용자명 또는 이메일로 사용자 조회
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        // 이메일 형식인지 확인
        if (usernameOrEmail.contains("@")) {
            return userRepository.findByEmailAndIsActiveTrue(usernameOrEmail)
                    .orElseThrow(() -> new AuthException.UserNotFoundException());
        } else {
            return userRepository.findByUsernameAndIsActiveTrue(usernameOrEmail)
                    .orElseThrow(() -> new AuthException.UserNotFoundException());
        }
    }

    /**
     * 사용자명 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 이메일 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 토큰 생성
     */
    private TokenResponseDto generateTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        long expiresIn = jwtTokenProvider.getRemainingTimeFromToken(accessToken) / 1000; // 초 단위

        return new TokenResponseDto(accessToken, refreshToken, expiresIn);
    }
}
