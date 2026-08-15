package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.UserDTO;
import com.example.Spring_Coffee_Shop_Project.entity.User;
import com.example.Spring_Coffee_Shop_Project.enumeration.UserStatus;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.UserRepository;
import com.example.Spring_Coffee_Shop_Project.service.EmailService;
import com.example.Spring_Coffee_Shop_Project.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserDTO getUserDetails(String username, String password) {

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new CustomerException(404, "Sorry no user found");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomerException(400, "Invalid password");
        }

        if (!user.isVerified()) {
            throw new CustomerException(401, "Please verify your email address before logging in!");
        }

        return mapToDTO(user);
    }
    @Override
    public List<UserDTO> getAllUsers() {

        log.info("Fetching all users...");
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();

        for (User user : users) {
            userDTOList.add(mapToDTO(user));
        }

        return userDTOList;
    }

    @Override
    public UserDTO getUserByUsername(String username) {

        log.info("Fetching user by username: {}", username);

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new CustomerException(404, "User not found with username: " + username);
        }

        User user = optionalUser.get();
        return mapToDTO(user);
    }

    @Override
    public void updateUser(UserDTO userDTO) {

        log.info("Executing Full Update for User ID: {}", userDTO.getUserId());

        Optional<User> updateUser = userRepository.findById(userDTO.getUserId());

        if (updateUser.isEmpty()){
            throw new CustomerException(404, "User not found with id " + userDTO.getUserId());
        }

        User user = updateUser.get();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(Integer.parseInt(userDTO.getPhone()));
        user.setUserstatus(userDTO.getUserstatus());
        user.setUserRole(userDTO.getUserRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(user);
    }

    @Override
    public void patchUser(Long id, UserDTO userDTO) {

        log.info("Executing Update (Patch) for User ID: {}", id);

        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new CustomerException(404, "User not found with id " + id);
        }

        User user = optionalUser.get();

        if (userDTO.getFirstName() != null) user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) user.setLastName(userDTO.getLastName());
        if (userDTO.getPhone() != null) user.setPhone(Integer.parseInt(userDTO.getPhone()));
        if (userDTO.getUserstatus() != null) user.setUserstatus(userDTO.getUserstatus());
        if (userDTO.getUserRole() != null) user.setUserRole(userDTO.getUserRole());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {

        log.info("Executing Soft Delete for User ID: {}", id);

        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new CustomerException(404, "User not found with id " + id);
        }

        User user = optionalUser.get();
        user.setUserstatus(UserStatus.INACTIVE);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void saveUser(UserDTO userDTO) {

        log.info("Execute User Save method...!");

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(Integer.parseInt(userDTO.getPhone()));

        user.setUserstatus(userDTO.getUserstatus() != null ? userDTO.getUserstatus() : UserStatus.ACTIVE);
        user.setUserRole(userDTO.getUserRole());

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerified(false);

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getUsername(), token);
    }

    //  Email Verification Endpoint
    @Override
    public boolean verifyUser(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(String.valueOf(user.getPhone()))
                .userstatus(user.getUserstatus())
                .userRole(user.getUserRole())
                .build();
    }
}