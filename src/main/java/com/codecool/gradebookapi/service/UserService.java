package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.StudentDto;
import com.codecool.gradebookapi.dto.TeacherDto;
import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.dataTypes.InitialCredentials;
import com.codecool.gradebookapi.dto.mapper.UserMapper;
import com.codecool.gradebookapi.exception.DuplicateAccountException;
import com.codecool.gradebookapi.exception.IncorrectPasswordException;
import com.codecool.gradebookapi.exception.UserNotFoundException;
import com.codecool.gradebookapi.exception.UsernameTakenException;
import com.codecool.gradebookapi.model.ApplicationUser;
import com.codecool.gradebookapi.model.SchoolActorApplicationUserRelation;
import com.codecool.gradebookapi.model.request.PasswordChangeRequest;
import com.codecool.gradebookapi.repository.SchoolActorApplicationUserRelationRepository;
import com.codecool.gradebookapi.repository.UserRepository;
import com.codecool.gradebookapi.security.ApplicationUserRole;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

import static com.codecool.gradebookapi.security.ApplicationUserRole.*;

@Service
public class UserService implements UserDetailsService {
    public static final int PASSWORD_LENGTH = 12;
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
//        userRepository.save(new ApplicationUser("admin", this.passwordEncoder.encode("admin"), ApplicationUserRole.ADMIN));
//        userRepository.save(new ApplicationUser("teacher", this.passwordEncoder.encode("teacher"), ApplicationUserRole.TEACHER));
//        userRepository.save(new ApplicationUser("student", this.passwordEncoder.encode("student"), STUDENT));
    }

    public List<UserDto> findAll() {
        return mapper.mapAll(userRepository.findAll());
    }

    public UserDto save(UserDto userDto) {
        ApplicationUser userToSave = mapper.map(userDto);
        ApplicationUser saved = userRepository.save(userToSave);

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
            throw new DuplicateAccountException(role);

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
        if (isUsernameAlreadyTaken(username)) throw new UsernameTakenException(username);

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
            generatedUsername = removeAccents(name.toLowerCase().replaceAll("[ .\\-']", "")) + RandomStringUtils.randomNumeric(2);
        } while (isUsernameAlreadyTaken(generatedUsername));

        return generatedUsername;
    }

    private String removeAccents(String name) {
        name = Normalizer.normalize(name, Normalizer.Form.NFD);

        return name.replaceAll("\\p{M}", "");
    }

    private boolean isUsernameAlreadyTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(PASSWORD_LENGTH);
    }

    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(mapper::map);
    }

    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(mapper::map);
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        ApplicationUser user = userRepository.getById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            throw new IncorrectPasswordException();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public void setUserDisabled(Long id) {
        ApplicationUser user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void setUserEnabled(Long id) {
        ApplicationUser user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public Optional<UserDto> getUserRelatedToSchoolActor(ApplicationUserRole role, Long schoolActorId) {
        if (role.equals(ADMIN)) return Optional.empty();

        Optional<SchoolActorApplicationUserRelation> relation =
                relationRepository.getByUserRoleAndSchoolActorId(role, schoolActorId);
        if (relation.isEmpty()) return Optional.empty();

        ApplicationUser user = userRepository.getById(relation.get().getAppUserId());

        return Optional.of(mapper.map(user));
    }

    public Long findStudentIdByUserId(Long userId) {
        SchoolActorApplicationUserRelation relation = relationRepository.findFirstByAppUserId(userId)
                .orElseThrow(() -> new RuntimeException("No school actor related to user exists"));
        if (!relation.getUserRole().equals(STUDENT)) throw new RuntimeException("User role is incorrect");
        return relation.getSchoolActorId();
    }

    public Long findTeacherIdByUserId(Long userId) {
        SchoolActorApplicationUserRelation relation = relationRepository.findFirstByAppUserId(userId)
                .orElseThrow(() -> new RuntimeException("No school actor related to user exists"));
        if (!relation.getUserRole().equals(TEACHER)) throw new RuntimeException("User role is incorrect");
        return relation.getSchoolActorId();
    }

    public Long getStudentIdOfCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDto user = findByUsername(username)
                // this should not happen
                .orElseThrow(() -> new RuntimeException(String.format("No user with the name '%s' exists", username)));

        return findStudentIdByUserId(user.getId());
    }

    public Long getTeacherIdOfCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDto user = findByUsername(username)
                // this should not happen
                .orElseThrow(() -> new RuntimeException(String.format("No user with the name '%s' exists", username)));

        return findTeacherIdByUserId(user.getId());
    }

    public ApplicationUserRole getRoleOfCurrentUser() {
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to determine user role"));

        return ApplicationUserRole.valueOf(role.substring("ROLE_".length()));
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with username \"%s\"", username)));
    }
}
