package com.example.userregistration.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(UserRegistrationRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User();
        copyProfile(
                user,
                request.firstName(),
                request.lastName(),
                email,
                request.dateOfBirth(),
                request.phoneNumber(),
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.stateProvince(),
                request.postalCode(),
                request.country()
        );
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findUser(id);
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException();
        }

        copyProfile(
                user,
                request.firstName(),
                request.lastName(),
                email,
                request.dateOfBirth(),
                request.phoneNumber(),
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.stateProvince(),
                request.postalCode(),
                request.country()
        );
        user.setActive(request.active());

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional
    public void delete(Long id) {
        User user = findUser(id);
        userRepository.delete(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void copyProfile(
            User user,
            String firstName,
            String lastName,
            String email,
            java.time.LocalDate dateOfBirth,
            String phoneNumber,
            String addressLine1,
            String addressLine2,
            String city,
            String stateProvince,
            String postalCode,
            String country
    ) {
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email);
        user.setDateOfBirth(dateOfBirth);
        user.setPhoneNumber(cleanOptionalText(phoneNumber));
        user.setAddressLine1(cleanOptionalText(addressLine1));
        user.setAddressLine2(cleanOptionalText(addressLine2));
        user.setCity(cleanOptionalText(city));
        user.setStateProvince(cleanOptionalText(stateProvince));
        user.setPostalCode(cleanOptionalText(postalCode));
        user.setCountry(cleanOptionalText(country));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
