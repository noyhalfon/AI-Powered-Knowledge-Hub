package com.knowledgehub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.knowledgehub.models.User;
import com.knowledgehub.models.UserRole;
import com.knowledgehub.repositories.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Save a new user with username and role to the database
     * @param user User entity containing username and role
     * @return Saved User entity
     */
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Ensure ID is null for new user (will be auto-generated)
        user.setId(null);
        
        return userRepository.save(user);
    }

    /**
     * Change user role by username
     * @param username The username of the user
     * @param newRole The new role to assign
     * @return Number of affected rows (should be 1 if successful)
     */
    public int changeRoleByName(String username, UserRole newRole) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        
        // Check if user exists
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }
        
        return userRepository.changeRoleByUsername(username, newRole);
    }

    /**
     * Change user role by user ID
     * @param userId The ID of the user
     * @param newRole The new role to assign
     * @return Number of affected rows (should be 1 if successful)
     */
    public int changeRoleById(Long userId, UserRole newRole) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        return userRepository.changeRole(userId, newRole);
    }

    /**
     * Get user by ID
     * @param id The user ID
     * @return User entity
     * @throws IllegalArgumentException if user not found
     */
    public User getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    /**
     * Get user role by username (case-insensitive)
     * @param userName The username
     * @return UserRole (ADMIN or USER)
     * @throws IllegalArgumentException if user not found
     */
    public UserRole getUserRole(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        Optional<UserRole> role = userRepository.getUserRoleByName(username);
        return role.orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    /**
     * Get all users and admins (returns all users regardless of role)
     * @return List of all users
     */
    public List<User> getAllUsersAndAdmins() {
        return userRepository.findAll();
    }

    /**
     * Get user by username (case-insensitive)
     * @param username The username
     * @return User entity
     * @throws IllegalArgumentException if user not found
     */
    public User getUserByName(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        return user.orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        return userRepository.existsByUsername(username);
    }
}
