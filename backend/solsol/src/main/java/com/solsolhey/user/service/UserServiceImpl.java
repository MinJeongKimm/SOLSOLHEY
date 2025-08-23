package com.solsolhey.user.service;

import com.solsolhey.user.dto.response.UserResponse;
import com.solsolhey.user.dto.request.UserUpdateRequest;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;
import com.solsolhey.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 서비스 구현체
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        return convertToUserResponse(user);
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                   .map(this::convertToUserResponse)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<UserResponse> searchUsersByNickname(String nickname) {
        List<User> users = userRepository.findByNickname(nickname);
        return users.stream()
                   .map(this::convertToUserResponse)
                   .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        if (request.nickname() != null && !request.nickname().isBlank()) {
            user.updateNickname(request.nickname());
        }
        
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }
    
    @Override
    @Transactional
    public UserResponse addPoints(Long userId, Integer points) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        user.addPoints(points);
        User savedUser = userRepository.save(user);
        
        log.info("사용자 {}에게 {}포인트 추가. 현재 총 포인트: {}", userId, points, savedUser.getTotalPoints());
        
        return convertToUserResponse(savedUser);
    }
    
    @Override
    @Transactional
    public UserResponse deductPoints(Long userId, Integer points) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        user.deductPoints(points);
        User savedUser = userRepository.save(user);
        
        log.info("사용자 {}에게서 {}포인트 차감. 현재 총 포인트: {}", userId, points, savedUser.getTotalPoints());
        
        return convertToUserResponse(savedUser);
    }
    
    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        user.deactivate();
        userRepository.save(user);
        
        log.info("사용자 {} 계정이 비활성화되었습니다", userId);
    }
    
    @Override
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        user.activate();
        userRepository.save(user);
        
        log.info("사용자 {} 계정이 활성화되었습니다", userId);
    }
    
    /**
     * User 엔티티를 UserResponse DTO로 변환
     */
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getNickname(),
            user.getCampus(),
            user.getTotalPoints(),
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
