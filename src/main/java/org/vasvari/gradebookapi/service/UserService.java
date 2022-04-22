package org.vasvari.gradebookapi.service;

import org.vasvari.gradebookapi.dto.StudentDto;
import org.vasvari.gradebookapi.dto.TeacherDto;
import org.vasvari.gradebookapi.dto.UserDto;
import org.vasvari.gradebookapi.dto.simpleTypes.InitialCredentials;
import org.vasvari.gradebookapi.dto.mapper.UserMapper;
import org.vasvari.gradebookapi.exception.DuplicateAccountException;
import org.vasvari.gradebookapi.exception.IncorrectPasswordException;
import org.vasvari.gradebookapi.exception.UserNotFoundException;
import org.vasvari.gradebookapi.exception.UsernameTakenException;
import org.vasvari.gradebookapi.model.ApplicationUser;
import org.vasvari.gradebookapi.model.SchoolActorApplicationUserRelation;
import org.vasvari.gradebookapi.model.request.PasswordChangeRequest;
import org.vasvari.gradebookapi.model.request.UserRequest;
import org.vasvari.gradebookapi.model.specification.UserSpecification;
import org.vasvari.gradebookapi.repository.SchoolActorApplicationUserRelationRepository;
import org.vasvari.gradebookapi.repository.UserRepository;
import org.vasvari.gradebookapi.security.ApplicationUserRole;
import org.apache.commons.lang3.RandomStringUtils;
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

import static org.vasvari.gradebookapi.security.ApplicationUserRole.*;

@Service
public class UserService implements UserDetailsService {
    public static final int PASSWORD_LENGTH = 12;
    private final UserRepository userRepository;
    private final SchoolActorApplicationUserRelationRepository relationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final UserSpecification specification;

    public UserService(UserRepository userRepository,
                       SchoolActorApplicationUserRelationRepository relationRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper mapper,
                       UserSpecification specification) {
        this.userRepository = userRepository;
        this.relationRepository = relationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.specification = specification;
    }

    public List<UserDto> findAll() {
        return mapper.mapAll(userRepository.findAll());
    }

    public List<UserDto> findUsers(UserRequest request) {
        return mapper.mapAll(userRepository.findAll(specification.getUsers(request)));
    }

    public UserDto save(UserDto userDto) {
        ApplicationUser userToSave = mapper.map(userDto);
        ApplicationUser saved = userRepository.save(userToSave);

        return mapper.map(saved);
    }

    public InitialCredentials createStudentUser(StudentDto studentDto) {
        String name = String.format("%s %s", studentDto.getLastname(), studentDto.getFirstname());
        return createNonAdminUser(studentDto.getId(), name, STUDENT);
    }

    public InitialCredentials createTeacherUser(TeacherDto teacherDto) {
        String name = String.format("%s %s", teacherDto.getLastname(), teacherDto.getFirstname());
        return createNonAdminUser(teacherDto.getId(), name, TEACHER);
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

    public void changePasswordOfCurrentUser(PasswordChangeRequest request) {
        long userId = getCurrentUser().getId();
        changePassword(userId, request);
    }

    public void deleteById(Long id) {
        relationRepository.findFirstByAppUserId(id)
                .ifPresent(relation -> relationRepository.deleteById(relation.getId()));
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

    public Optional<UserDto> getUserRelatedToStudent(Long studentId) {
        return getUserRelatedToSchoolActor(STUDENT, studentId);
    }

    public Optional<UserDto> getUserRelatedToTeacher(Long teacherId) {
        return getUserRelatedToSchoolActor(TEACHER, teacherId);
    }

    private Optional<UserDto> getUserRelatedToSchoolActor(ApplicationUserRole role, Long schoolActorId) {
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
        UserDto user = getCurrentUser();
        return findStudentIdByUserId(user.getId());
    }

    public Long getTeacherIdOfCurrentUser() {
        UserDto user = getCurrentUser();
        return findTeacherIdByUserId(user.getId());
    }

    private UserDto getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username)
                // this should not happen
                .orElseThrow(() -> new RuntimeException(String.format("No user with the name '%s' exists", username)));
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with username \"%s\"", username)));
    }
}
