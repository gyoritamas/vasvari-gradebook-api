package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.mapper.UserMapper;
import com.codecool.gradebookapi.model.User;
import com.codecool.gradebookapi.repository.UserRepository;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        // TODO: remove
        repository.save(new User(1L, "admin", passwordEncoder.encode("admin"), ApplicationUserRole.ADMIN));
        repository.save(new User(2L, "teacher", passwordEncoder.encode("teacher"), ApplicationUserRole.TEACHER));
        repository.save(new User(3L, "student", passwordEncoder.encode("student"), ApplicationUserRole.STUDENT));
    }

    public List<UserDto> findAll() {
        return mapper.mapAll(repository.findAll());
    }

    public UserDto save(UserDto userDto) {
        User userToSave = mapper.map(userDto);
        User saved = repository.save(userToSave);

        return mapper.map(saved);
    }

    public Optional<UserDto> findById(Long id) {
        return repository.findById(id).map(user -> mapper.map(user));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with username \"%s\"", username)));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.getRole().getGrantedAuthorities());
    }
}
