package com.example.Spring_Coffee_Shop_Project.security;

import com.example.Spring_Coffee_Shop_Project.enumeration.UserStatus;
import com.example.Spring_Coffee_Shop_Project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.Spring_Coffee_Shop_Project.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Check if the user's status is ACTIVE
        boolean isActive = user.getUserstatus() == UserStatus.ACTIVE;

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!isActive) // Disables the account if status is not ACTIVE (blocks login for INACTIVE/SUSPENDED users)
                .roles(user.getUserRole().name())
                .build();
    }
}