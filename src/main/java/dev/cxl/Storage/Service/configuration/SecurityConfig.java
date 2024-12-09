package dev.cxl.Storage.Service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final String[] SWAGGER_ENDPOINT = {
        "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui/index.html", "users/confirmCreateUser"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request -> request.requestMatchers(SWAGGER_ENDPOINT)
                .permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/private/**").authenticated()
                .anyRequest()
                .permitAll());
        //
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .jwt(Customizer.withDefaults()));
        httpSecurity.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());
        return httpSecurity.build();
    }
}
