package com.example.oauthjwt.config;

import com.example.oauthjwt.CustomOAuth2AuthorizedClientService;
import com.example.oauthjwt.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2AuthorizedClientService customOAuth2AuthorizedClientService;
    private final ClientRegistrationRepository customClientRegistrationRepo;
    private final JdbcTemplate jdbcTemplate;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomOAuth2AuthorizedClientService customOAuth2AuthorizedClientService, ClientRegistrationRepository customClientRegistrationRepo, JdbcTemplate jdbcTemplate) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOAuth2AuthorizedClientService = customOAuth2AuthorizedClientService;
        this.customClientRegistrationRepo = customClientRegistrationRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //csrf disable
        http
                .csrf(AbstractHttpConfigurer::disable);
        //From 로그인 방식 disable
        http
                .formLogin(AbstractHttpConfigurer::disable);
        //HTTP Basic 인증 방식 disable
        http
                .httpBasic(AbstractHttpConfigurer::disable);
        //oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                        .authorizedClientService(customOAuth2AuthorizedClientService.oAuth2AuthorizedClientService(jdbcTemplate, customClientRegistrationRepo))
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService)));

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated());

        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
