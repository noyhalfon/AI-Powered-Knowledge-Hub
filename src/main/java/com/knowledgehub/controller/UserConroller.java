package com.knowledgehub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.knowledgehub.models.User;
import com.knowledgehub.models.UserRole;
import com.knowledgehub.models.DTO.ErrorResponse;
import com.knowledgehub.models.DTO.ChangeRoleRequest;
import com.knowledgehub.services.UserService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/user")
public class UserConroller {

    @Autowired
    private UserService userService;

    @GetMapping("/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsersAndAdmins();
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/createUser")
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/changeRole/{id}")
    public ResponseEntity<Object> changeRoleByID(
            @PathVariable("id") Long id, 
            @RequestBody ChangeRoleRequest request) {
        try {
            if (request == null || request.getRole() == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Role is required"));
            }
            userService.changeRoleById(id, request.getRole());
            // Fetch and return the updated user
            User updatedUser = userService.getUserById(id);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/changeRoleByName/{username}")
    public ResponseEntity<Object> changeRoleByName(
            @PathVariable("username") String username, 
            @RequestBody ChangeRoleRequest request) {
        try {
            if (request == null || request.getRole() == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Role is required"));
            }
            userService.changeRoleByName(username, request.getRole());
            // Fetch and return the updated user
            User updatedUser = userService.getUserByName(username);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/getUserRole/{username}")
    public ResponseEntity<Object> getUserRole(@PathVariable("username") String username) {
        try {
            UserRole role = userService.getUserRole(username);
            return ResponseEntity.ok(role);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/getUserByName/{username}")
    public ResponseEntity<Object> getUserByName(@PathVariable("username") String username) {
        try {
            User user = userService.getUserByName(username);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/existsByUsername/{username}")
    public ResponseEntity<Object> existsByUsername(@PathVariable("username") String username) {
        try {
            Boolean exists = userService.existsByUsername(username);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
