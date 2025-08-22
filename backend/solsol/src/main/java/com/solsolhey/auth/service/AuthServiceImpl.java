package com.solsolhey.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.auth.dto.request.LoginRequestDto;
import com.solsolhey.auth.dto.response.LoginSuccessDto;
import com.solsolhey.auth.dto.request.SignUpRequestDto;
import com.solsolhey.auth.dto.response.SignUpResponse;
import com.solsolhey.auth.dto.response.TokenResponseDto;
import com.solsolhey.auth.jwt.JwtTokenProvider;
import com.solsolhey.common.exception.AuthException;
import com.solsolhey.common.exception.BusinessException;
import com.solsolhey.common.response.ApiResponse;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증/인가 관련 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UsernameGenerator usernameGenerator;

    /**
     * 회원가입
     */
    @Override
    public SignUpResponse signUp(SignUpRequestDto requestDto) {
        log.info("회원가입 시도: userId={}", requestDto.getUserId());

        // 중복 검사
        validateDuplicateUser(requestDto.getUserId());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 자동 username 생성
        String autoUsername = usernameGenerator.generateUsername(requestDto.getUserId());

        // 사용자 생성
        User user = User.builder()
                .username(autoUsername)           // 자동 생성된 username
                .email(requestDto.getUserId())    // userId를 email로 사용
                .passwordHash(encodedPassword)
                .nickname(requestDto.getNickname())
                .campus(requestDto.getCampus())   // 클라이언트에서 받은 campus 값 사용
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, username={}, nickname={}", 
                savedUser.getUserId(), savedUser.getUsername(), savedUser.getNickname());

        return SignUpResponse.success("회원가입이 완료되었습니다.", savedUser.getUserId());
    }

    /**
     * 로그인
     */
    @Override
    public ApiResponse<LoginSuccessDto> login(LoginRequestDto requestDto) {
        log.info("로그인 시도: userId={}", requestDto.userId());

        try {
            // 사용자 조회 (userId는 이메일 형식)
            User user = findUserByEmail(requestDto.userId());
            
            // 인증 수행
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            requestDto.password()
                    )
            );

            // Access Token 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername());
            
            log.info("로그인 완료: userId={}, username={}", user.getUserId(), user.getUsername());
            
            LoginSuccessDto loginData = new LoginSuccessDto(accessToken, user.getUsername());
            return ApiResponse.success("로그인 되었습니다.", loginData);

        } catch (AuthException.UserNotFoundException e) {
            log.warn("로그인 실패: userId={}, 원인=사용자 없음", requestDto.userId());
            return ApiResponse.unauthorized("로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.");
        } catch (AuthenticationException e) {
            log.warn("로그인 실패: userId={}, 원인={}", requestDto.userId(), e.getMessage());
            return ApiResponse.unauthorized("로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.");
        }
    }

    /**
     * 토큰 갱신
     */
    @Override
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
     * 로그아웃 (토큰 블랙리스트 처리)
     * 토큰을 블랙리스트에 추가하여 재사용을 방지
     */
    @Override
    public void logout(String accessToken) {
        log.info("로그아웃 처리 시작");
        
        try {
            // 토큰 유효성 기본 검증 (만료된 토큰도 블랙리스트에 추가)
            if (accessToken == null || accessToken.isBlank()) {
                log.warn("로그아웃 시도: 빈 토큰");
                return;
            }
            
            // 사용자 정보 추출 (로그 용도)
            try {
                Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
                String username = jwtTokenProvider.getUsernameFromToken(accessToken);
                log.info("로그아웃 사용자: userId={}, username={}", userId, username);
            } catch (Exception e) {
                log.debug("토큰에서 사용자 정보 추출 실패 (만료된 토큰일 수 있음): {}", e.getMessage());
            }
            
            // 토큰을 블랙리스트에 추가
            jwtTokenProvider.blacklistToken(accessToken);
            
            log.info("로그아웃 완료: 토큰이 블랙리스트에 추가됨");
            
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            // 로그아웃은 실패해도 클라이언트에서 토큰을 제거하므로 예외를 던지지 않음
        }
    }

    /**
     * 사용자명 중복 체크 (userId는 이메일 형식이므로 이메일 중복 체크)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByEmail(username);
    }

    /**
     * 이메일 중복 체크 (userId 중복 체크와 동일)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
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
     * 토큰 생성
     */
    private TokenResponseDto generateTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        long expiresIn = jwtTokenProvider.getRemainingTimeFromToken(accessToken) / 1000; // 초 단위

        return new TokenResponseDto(accessToken, refreshToken, expiresIn);
    }
}
