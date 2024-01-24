package com.tuliomssantos.services;

import org.springframework.stereotype.Service;
import com.tuliomssantos.models.RegistrationSource;
import com.tuliomssantos.models.UserModel;
import com.tuliomssantos.models.UserRole;
import com.tuliomssantos.repositories.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final Validator validator;

    public UserService(UserRepository userRepository, Validator validator) {
        this.userRepository = userRepository;

        this.validator = validator;
    }

    public List<UserModel> getUsers() {
        return userRepository.findAll();
    }

    public Optional<UserModel> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void save(UserModel user) {
        userRepository.save(user);
    }

    private void validateUser(UserModel user) {
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Validation failed for UserModel", violations);
        }
    }

    @Transactional
    public UserModel register(String name, String email, UserRole role, RegistrationSource source) {
        UserModel newUser = new UserModel();

        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setSource(source);

        validateUser(newUser);

        return userRepository.save(newUser);
    }
}
