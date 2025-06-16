package com.example.registration.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class AdminUser implements UserDetails {

    private final Student student;

    public AdminUser(Student student) {
        this.student = student;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // there are no roles for this app
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return student.getPassword();
    }

    @Override
    public String getUsername() {
        // using student email as username
        return student.getEmail();
    }

    // user account has not expired
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    // user is not locked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // password is not expired
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // enable user
    @Override
    public boolean isEnabled() {
        return true;
    }

}