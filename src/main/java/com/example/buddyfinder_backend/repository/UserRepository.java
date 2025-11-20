package com.example.buddyfinder_backend.repository;

import com.example.buddyfinder_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByIsAdminTrue();
    long countByIsSuperAdminTrue();
    boolean existsByIsSuperAdminTrueAndUserIdNot(Long userId);
}
