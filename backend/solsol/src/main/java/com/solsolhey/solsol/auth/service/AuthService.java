package com.solsolhey.solsol.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.solsol.auth.dto.LoginRequestDto;
import com.solsolhey.solsol.auth.dto.LoginResponse;
import com.solsolhey.solsol.auth.dto.SignUpRequestDto;
import com.solsolhey.solsol.auth.dto.SignUpResponse;
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
    public SignUpResponse signUp(SignUpRequestDto requestDto) {
        log.info("회원가입 시도: userId={}", requestDto.getUserId());

        // 중복 검사
        validateDuplicateUser(requestDto.getUserId());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 사용자 생성 (userId를 email과 username 모두에 설정)
        User user = User.builder()
                .username(requestDto.getUserId()) // userId를 username으로 사용
                .email(requestDto.getUserId())    // userId를 email로 사용
                .passwordHash(encodedPassword)
                .nickname(requestDto.getNickname())
                .campus(null) // campus는 더 이상 사용하지 않음
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        return SignUpResponse.success("회원가입이 완료되었습니다.", savedUser.getUserId());
    }

    /**
     * 로그인
     */
    public LoginResponse login(LoginRequestDto requestDto) {
        log.info("로그인 시도: userId={}", requestDto.getUserId());

        try {
            // 사용자 조회 (userId는 이메일 형식)
            User user = findUserByEmail(requestDto.getUserId());
            
            // 인증 수행
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            requestDto.getPassword()
                    )
            );

            // Access Token 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername());
            
            log.info("로그인 완료: userId={}, username={}", user.getUserId(), user.getUsername());
            
            return LoginResponse.success(
                "로그인 되었습니다.", 
                accessToken, 
                user.getUsername() // 로그인한 유저의 ID (실제로는 username)
            );

        } catch (AuthException.UserNotFoundException e) {
            log.warn("로그인 실패: userId={}, 원인=사용자 없음", requestDto.getUserId());
            return LoginResponse.authFailure("로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.");
        } catch (AuthenticationException e) {
            log.warn("로그인 실패: userId={}, 원인={}", requestDto.getUserId(), e.getMessage());
            return LoginResponse.authFailure("로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.");
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
     * 중복 사용자 검증 (userId는 이메일 형식)
     */
    private void validateDuplicateUser(String userId) {
        if (userRepository.existsByEmail(userId)) {
            throw new BusinessException("이미 사용중인 아이디입니다.");
        }
    }

    /**
     * 이메일로 사용자 조회
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new AuthException.UserNotFoundException());
    }

    /**
     * 사용자명 중복 체크 (userId는 이메일 형식이므로 이메일 중복 체크)
     */
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByEmail(username);
    }

    /**
     * 이메일 중복 체크 (userId 중복 체크와 동일)
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
