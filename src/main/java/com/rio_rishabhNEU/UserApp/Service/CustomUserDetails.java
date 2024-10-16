package com.rio_rishabhNEU.UserApp.Service;

import com.rio_rishabhNEU.UserApp.Model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Return the password stored in your User entity
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Return the email as the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // You can add logic here if you have an "account expiration" feature
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add logic if you have a "account lock" feature
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add logic for credentials expiration if necessary
    }

    @Override
    public boolean isEnabled() {
        return true; // You can add a "enabled" flag to the User entity and return its value here
    }

    public User getUser() {
        return user; // This method is optional, but it allows you to access the full User entity if needed
    }
}

