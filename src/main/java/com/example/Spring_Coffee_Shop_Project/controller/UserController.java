package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.constant.CommonResponse;
import com.example.Spring_Coffee_Shop_Project.dto.AuthDTO;
import com.example.Spring_Coffee_Shop_Project.dto.UserDTO;
import com.example.Spring_Coffee_Shop_Project.enumeration.UserRole;
import com.example.Spring_Coffee_Shop_Project.enumeration.UserStatus;
import com.example.Spring_Coffee_Shop_Project.security.JwtUtil;
import com.example.Spring_Coffee_Shop_Project.service.LoginHistoryService;
import com.example.Spring_Coffee_Shop_Project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final LoginHistoryService loginHistoryService;

    // Register Endpoint (User Save)
    @PostMapping("/register")
    public ResponseEntity<CommonResponse> registerUser(@RequestBody UserDTO userDTO){
        userService.saveUser(userDTO);
        return ResponseEntity.ok(new CommonResponse(200,"User Registered Successfully"));
    }

    // Login Endpoint
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> login(@RequestBody AuthDTO authDTO, HttpServletRequest request){
        UserDTO userDetails = userService.getUserDetails(authDTO.getUsername(), authDTO.getPassword());

        loginHistoryService.recordLogin(userDetails, request);

        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new CommonResponse(200, token));
    }

    // Test Protected Endpoint
    @GetMapping("/me")
    public ResponseEntity<String> getProfile() {
        return ResponseEntity.ok("Access Granted: Valid Token!");
    }

    // Get All Users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get User By Username
    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username){
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    // Full Update (PUT)
    @PutMapping
    public ResponseEntity<CommonResponse> updateUser(@RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO);
        return ResponseEntity.ok(new CommonResponse(200, "User updated successfully"));
    }

    // Partial Update (PATCH)
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse> patchUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userService.patchUser(id, userDTO);
        return ResponseEntity.ok(new CommonResponse(200, "User partially updated successfully"));
    }

    // Soft Delete User
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new CommonResponse(200, "User status updated to INACTIVE successfully"));
    }

    // Only Using Pass the Frontend UserRoles
    @GetMapping("/roles")
    public ResponseEntity<UserRole[]> getUserRoles() {
        return ResponseEntity.ok(UserRole.values());
    }

    // Only Using Pass the Frontend UserStatus
    @GetMapping("/statuses")
    public ResponseEntity<UserStatus[]> getUserStatuses() {
        return ResponseEntity.ok(UserStatus.values());
    }

    // Verify Email
    @GetMapping("/verify")
    public ResponseEntity<CommonResponse> verifyUser(@RequestParam("token") String token) {
        boolean isVerified = userService.verifyUser(token);

        if (isVerified) {
            return ResponseEntity.ok(new CommonResponse(200, "Email verification successful! You can now log in."));
        } else {
            return ResponseEntity.status(400).body(new CommonResponse(400, "Invalid or expired verification token!"));
        }
    }
}
