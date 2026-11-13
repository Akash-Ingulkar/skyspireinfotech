package com.skyspireinfotech.admin.config;

import com.skyspireinfotech.admin.security.AdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(AdminUserDetailsService userDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            // The public website's contact form posts JSON here anonymously (no session, no
            // privileges), so a CSRF token isn't applicable. All admin pages keep CSRF protection.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/contact"))
            // The whole app is served under server.servlet.context-path=/admin, so these
            // paths are relative to it (e.g. "/dashboard" is skyspireinfotech.com/admin/dashboard).
            // Everything that isn't explicitly public requires the ADMIN role.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/assets/**", "/uploads/**", "/login", "/api/**").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .formLogin((FormLoginConfigurer<HttpSecurity> form) -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
