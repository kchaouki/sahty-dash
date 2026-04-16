package com.sahty.fs.security;

import com.sahty.fs.entity.User;
import com.sahty.fs.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        // Add system role
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().name()));
        // Add all permissions from direct assignments
        user.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        // Add all permissions from roles
        user.getRoles().forEach(role ->
                role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p))));

        return new SahtyUserPrincipal(user, authorities);
    }
}
