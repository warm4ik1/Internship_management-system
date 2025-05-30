package org.warm4ik.ims.security.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.warm4ik.ims.entity.User;

import java.util.Collection;
import java.util.stream.Collectors;

public record CustomUserDetails(User user) implements UserDetails {
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getTitle()))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return UserDetails.super.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return UserDetails.super.isEnabled();
  }

  public Long getId() {
    return user.getId();
  }
}
