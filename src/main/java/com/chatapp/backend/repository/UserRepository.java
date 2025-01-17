package com.chatapp.backend.repository;

import com.chatapp.backend.entity.User;
import com.chatapp.backend.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username with preferences
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.preferences " +
            "WHERE u.username = :username " +
            "AND u.deleted = false")
    Optional<User> findByUsername(@Param("username") String username);

    // Find user by email with preferences
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.preferences " +
            "WHERE u.email = :email " +
            "AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    // Search users by username, display name, or email
    @Query("SELECT u FROM User u " +
            "WHERE u.deleted = false " +
            "AND u.isActive = true " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY " +
            "CASE WHEN LOWER(u.username) = LOWER(:query) THEN 0 " +
            "     WHEN LOWER(u.username) LIKE LOWER(CONCAT(:query, '%')) THEN 1 " +
            "     ELSE 2 END, " +
            "u.username")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    // Update refresh token
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :token WHERE u.id = :userId")
    void updateRefreshToken(
            @Param("userId") Long userId,
            @Param("token") String token);

    // Find users by status
    @Query("SELECT u FROM User u " +
            "WHERE u.status = :status " +
            "AND u.deleted = false " +
            "AND u.isActive = true")
    List<User> findByStatus(@Param("status") UserStatus status);

    // Find recently active users
    @Query("SELECT u FROM User u " +
            "WHERE u.status = :status " +
            "AND u.lastSeenAt > :time " +
            "AND u.deleted = false " +
            "AND u.isActive = true " +
            "ORDER BY u.lastSeenAt DESC")
    List<User> findByStatusAndLastSeenAtAfter(
            @Param("status") UserStatus status,
            @Param("time") Instant time);

    // Update user status
    @Modifying
    @Query("UPDATE User u SET " +
            "u.status = :status, " +
            "u.lastSeenAt = :lastSeenAt " +
            "WHERE u.id = :userId " +
            "AND u.deleted = false")
    void updateUserStatus(
            @Param("userId") Long userId,
            @Param("status") UserStatus status,
            @Param("lastSeenAt") Instant lastSeenAt);

    // Find all active users
    @Query("SELECT u FROM User u " +
            "WHERE u.deleted = false " +
            "AND u.isActive = true " +
            "ORDER BY u.username")
    Page<User> findAllActiveUsers(Pageable pageable);

    // Find users by multiple usernames
    @Query("SELECT u FROM User u " +
            "WHERE u.username IN :usernames " +
            "AND u.deleted = false " +
            "AND u.isActive = true")
    List<User> findByUsernames(@Param("usernames") List<String> usernames);

    // Find inactive users
    @Query("SELECT u FROM User u " +
            "WHERE u.lastSeenAt < :inactiveDate " +
            "AND u.deleted = false " +
            "AND u.isActive = true")
    List<User> findInactiveUsers(@Param("inactiveDate") Instant inactiveDate);

    // Count online users
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.status = 'ONLINE' " +
            "AND u.deleted = false " +
            "AND u.isActive = true")
    long countOnlineUsers();

    // Update email verification status
    @Modifying
    @Query("UPDATE User u SET " +
            "u.emailVerified = :verified " +
            "WHERE u.id = :userId")
    void updateEmailVerificationStatus(
            @Param("userId") Long userId,
            @Param("verified") boolean verified);
}