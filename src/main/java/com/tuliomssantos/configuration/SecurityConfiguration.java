package com.tuliomssantos.configuration;

import com.tuliomssantos.models.RegistrationSource;
import com.tuliomssantos.models.UserModel;
import com.tuliomssantos.models.UserRole;
import com.tuliomssantos.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;

import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    private final OAuth2AuthenticationEntrypoint oAuth2AuthenticationEntrypoint;

    private final UserService userService;

    @Value("${frontend.url}")
    private String frontendUrl;

    public SecurityConfiguration(OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            OAuth2AuthenticationEntrypoint oAuth2AuthenticationEntrypoint, UserService userService,
            CustomLogoutSuccessHandler customLogoutSuccessHandler) {

        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;

        this.oAuth2AuthenticationEntrypoint = oAuth2AuthenticationEntrypoint;

        this.userService = userService;

        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable()).authorizeHttpRequests(auth -> {
                    auth.anyRequest().authenticated();
                }).logout(logout -> {

                    logout.logoutSuccessUrl(frontendUrl).permitAll();

                    logout.logoutUrl("/api/auth/logout");

                    logout.deleteCookies("JSESSIONID");

                    logout.clearAuthentication(true);

                    logout.invalidateHttpSession(true);

                    logout.logoutSuccessHandler(customLogoutSuccessHandler);

                }).exceptionHandling(exceptionHandlingConfigurer -> {
                    exceptionHandlingConfigurer
                            .authenticationEntryPoint(oAuth2AuthenticationEntrypoint);
                }).oauth2Login(oauth2 -> {
                    oauth2.userInfoEndpoint(userInfoEndpoint -> {
                        userInfoEndpoint.oidcUserService(oidcUserService());
                    });

                    oauth2.successHandler(oAuth2LoginSuccessHandler);
                }).build();
    }

    /**
     * Delegation-based strategy with OAuth2UserService
     * 
     * @see https://docs.spring.io/spring-security/site/docs/5.0.7.RELEASE/reference/html/oauth2login-advanced.html#oauth2login-advanced-map-authorities-oauth2userservice
     */
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);

            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            RegistrationSource registrationSource =
                    mapRegistrationIdToRegistrationSource(registrationId);

            Map<String, Object> attributes = oidcUser.getAttributes();

            String email = attributes.getOrDefault("email", "").toString();

            String name = attributes.getOrDefault("name", "").toString();

            Optional<UserModel> userModel = userService.findByEmail(email);

            if (userModel.isEmpty()) {
                userService.register(name, email, UserRole.ROLE_USER, registrationSource);
            } else {
                var user = userModel.get();

                var userAuthority = user.getRole().name();

                mappedAuthorities.add(new SimpleGrantedAuthority(userAuthority));
            }

            oidcUser = new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(),
                    oidcUser.getUserInfo());

            return oidcUser;
        };
    }

    private RegistrationSource mapRegistrationIdToRegistrationSource(String registrationId) {
        try {
            return RegistrationSource.valueOf(registrationId.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(frontendUrl));

        configuration.addAllowedHeader("*");

        configuration.addAllowedMethod("*");

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource =
                new UrlBasedCorsConfigurationSource();

        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);

        return urlBasedCorsConfigurationSource;

    }
}
