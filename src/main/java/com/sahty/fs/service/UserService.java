package com.sahty.fs.service;

import com.sahty.fs.entity.Role;
import com.sahty.fs.entity.User;
import com.sahty.fs.repository.TenantRepository;
import com.sahty.fs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findByTenant(String tenantId) {
        return userRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    @Transactional
    public User createUser(User user, String rawPassword, String tenantId) {
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà pris: " + user.getUsername());
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        if (tenantId != null) {
            tenantRepository.findById(tenantId).ifPresent(user::setTenant);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String userId, String newPassword) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(u);
        });
    }

    @Transactional
    public void deactivateUser(String id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
    }
}
