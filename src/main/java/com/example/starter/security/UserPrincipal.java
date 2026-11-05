package com.example.starter.security;

import com.example.starter.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Authenticated user. Authorities = ROLE_{role code} + every permission code of every role. */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final int tokenVersion;
    private final Set<GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String password, boolean enabled,
                         int tokenVersion, Set<GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.tokenVersion = tokenVersion;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getCode())));
        });
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.isEnabled(),
                user.getTokenVersion(), Set.copyOf(authorities));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
