package com.knowledgehub.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgehub.models.User;
import com.knowledgehub.models.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ========== Inherited from JpaRepository ==========
    // These methods are automatically available:
    // - save(User entity) - Save or update a user (saves username and role to DB)
    // - findById(Long id) - Find user by ID
    // - findAll() - Find all users (returns all users and admins)
    // - deleteById(Long id) - Delete user by ID
    // - delete(User entity) - Delete a user
    // - count() - Count all users
    // - existsById(Long id) - Check if user exists by ID
    // ===================================================
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by username (case-insensitive)
    Optional<User> findByUsernameIgnoreCase(String username);
    
    // Get user role by username (case-insensitive)
    @Query("SELECT u.role FROM User u WHERE UPPER(u.username) = UPPER(:username)")
    Optional<UserRole> getUserRoleByName(@Param("username") String userName);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Find all users by role
    List<User> findByRole(UserRole role);
    
    // Find users by username containing (case-insensitive)
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    // Count users by role
    long countByRole(UserRole role);
    
    // Custom query to find users by username pattern
    @Query("SELECT u FROM User u WHERE u.username LIKE %:pattern%")
    List<User> findByUsernamePattern(@Param("pattern") String pattern);
    
    // Custom query to find all admins
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();
    
    // Custom query to find all regular users
    @Query("SELECT u FROM User u WHERE u.role = 'USER'")
    List<User> findAllRegularUsers();
    
    // Change user role by user ID
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :newRole WHERE u.id = :userId")
    int changeRole(@Param("userId") Long userId, @Param("newRole") UserRole newRole);
    
    // Change user role by username
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :newRole WHERE u.username = :username")
    int changeRoleByUsername(@Param("username") String username, @Param("newRole") UserRole newRole);
}
