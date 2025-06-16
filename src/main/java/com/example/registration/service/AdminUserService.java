package com.example.registration.service;

import com.example.registration.model.AdminUser;
import com.example.registration.repository.StudentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService implements UserDetailsService {

    private final StudentRepository studentRepository;

    public AdminUserService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username will be the student's email
        return studentRepository
                // returns Optional<Student>
                .findByEmail(username)
                // take in the
                .map(AdminUser::new)
                // if no student is found in the database
                .orElseThrow(() -> new UsernameNotFoundException("Email not found: " + username));
    }
}