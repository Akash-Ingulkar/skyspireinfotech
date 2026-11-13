package com.skyspireinfotech.admin.security;

import com.skyspireinfotech.admin.entity.AdminUser;
import com.skyspireinfotech.admin.repository.AdminUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No admin user found: " + username));

        return User.withUsername(adminUser.getUsername())
                .password(adminUser.getPassword())
                .disabled(!adminUser.isEnabled())
                .roles("ADMIN")
                .build();
    }
}
