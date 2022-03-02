package com.codecool.gradebookapi.service;

import com.codecool.gradebookapi.dto.UserDto;
import com.codecool.gradebookapi.dto.mapper.UserMapper;
import com.codecool.gradebookapi.model.User;
import com.codecool.gradebookapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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
}
