package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.*;
import com.codecool.gradebookapi.dto.mapper.UserMapper;
import com.codecool.gradebookapi.model.SchoolActorApplicationUserRelation;
import com.codecool.gradebookapi.model.User;
import com.codecool.gradebookapi.repository.SchoolActorApplicationUserRelationRepository;
import com.codecool.gradebookapi.repository.UserRepository;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.codecool.gradebookapi.security.ApplicationUserRole.*;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final SchoolActorApplicationUserRelationRepository relationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserService(UserRepository userRepository,
                       SchoolActorApplicationUserRelationRepository relationRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper mapper) {
        this.userRepository = userRepository;
        this.relationRepository = relationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        // TODO: remove
        userRepository.save(new User(1L, "admin", this.passwordEncoder.encode("admin"), ApplicationUserRole.ADMIN));
        userRepository.save(new User(2L, "teacher", this.passwordEncoder.encode("teacher"), ApplicationUserRole.TEACHER));
        userRepository.save(new User(3L, "student", this.passwordEncoder.encode("student"), STUDENT));
    }

    public List<UserDto> findAll() {
        return mapper.mapAll(userRepository.findAll());
    }

    public UserDto save(UserDto userDto) {
        User userToSave = mapper.map(userDto);
        User saved = userRepository.save(userToSave);

        return mapper.map(saved);
    }

    public InitialCredentials createStudentUser(StudentDto studentDto) {
        return createNonAdminUser(studentDto.getId(), studentDto.getName(), STUDENT);
    }

    public InitialCredentials createTeacherUser(TeacherDto teacherDto) {
        return createNonAdminUser(teacherDto.getId(), teacherDto.getName(), TEACHER);
    }

    private InitialCredentials createNonAdminUser(Long id, String name, ApplicationUserRole role) {
        if (relationRepository.existsSchoolActorApplicationUserRelationByUserRoleAndSchoolActorId(role, id))
            // TODO: create custom exception
            throw new RuntimeException("Already has an account");

        String username = generateUsername(name);
        String password = generatePassword();
        UserDto newUser = new UserDto(username, passwordEncoder.encode(password), role);
        newUser = save(newUser);

        SchoolActorApplicationUserRelation relation = SchoolActorApplicationUserRelation.builder()
                .appUserId(newUser.getId())
                .userRole(role)
                .schoolActorId(id)
                .build();
        relationRepository.save(relation);

        return InitialCredentials.builder()
                .userId(newUser.getId())
                .username(username)
                .password(password)
                .build();
    }

    public InitialCredentials createAdminUser(String username) {

        String password = generatePassword();
        UserDto newUser = new UserDto(username, passwordEncoder.encode(password), ADMIN);
        newUser = save(newUser);

        return InitialCredentials.builder()
                .userId(newUser.getId())
                .username(username)
                .password(password)
                .build();
    }

    private String generateUsername(String name) {
        String generatedUsername;
        do {
            generatedUsername = name.toLowerCase().replaceAll(" ", "") + RandomStringUtils.randomNumeric(2);
        } while (isUsernameAlreadyTaken(generatedUsername));

        return generatedUsername;
    }

    public boolean isUsernameAlreadyTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(mapper::map);
    }

    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(mapper::map);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with username \"%s\"", username)));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.getRole().getGrantedAuthorities());
    }

}
