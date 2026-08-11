package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.UserDTO;
import com.example.Spring_Coffee_Shop_Project.entity.User;
import com.example.Spring_Coffee_Shop_Project.repository.UserRepository;
import com.example.Spring_Coffee_Shop_Project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDTO getUserDetails(String username, String password) {

        Optional<User> optionalUser = userRepository.findByUserNameAndPassword(username,password);
        if(optionalUser.isEmpty())
            throw new RuntimeException("Sorry no user");

        User user = optionalUser.get();
        return new UserDTO(user.getUserId(),user.getUserName(),user.getUserRole(),user.getPassword());

    }

    @Override
    public void saveUser(UserDTO userDTO) {

        try {
            User user = new User();
            user.setUserName(userDTO.getUsername());
            user.setPassword(userDTO.getPassword());
            user.setUserRole(userDTO.getUserRole());

            userRepository.save(user);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
