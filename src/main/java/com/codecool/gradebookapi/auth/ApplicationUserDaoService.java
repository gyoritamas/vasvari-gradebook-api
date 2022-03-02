package com.codecool.gradebookapi.auth;

import com.codecool.gradebookapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ApplicationUserDaoService implements ApplicationUserDao {

    private final UserRepository userRepository;

    @Autowired
    public ApplicationUserDaoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<ApplicationUser> selectApplicationUserByUsername(String username) {
        return getApplicationUsers().stream()
                .filter(applicationUser -> username.equals(applicationUser.getUsername()))
                .findFirst();
    }

    private List<ApplicationUser> getApplicationUsers() {
        return userRepository.findAll().stream().map(
                user -> new ApplicationUser(
                        user.getUsername(),
                        user.getPassword(),
                        user.getRole().getGrantedAuthorities(),
                        // TODO: use user's field values
                        true,
                        true,
                        true,
                        true
                )
        ).collect(Collectors.toList());
    }
}
