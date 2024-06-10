package com.example.springjwt.advanced.service;

import com.example.springjwt.advanced.dto.CustomUserDetails;
import com.example.springjwt.advanced.entity.UserEntity;
import com.example.springjwt.advanced.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userData = userRepository.findByUsername(username);

        if(userData != null){
            // UserDetails에 담아서 return하면 AuthenticationManager가 검증함
            return new CustomUserDetails(userData);
        }
        return null;
    }
}
