package com.example.bth2jwt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bth2jwt.entity.User;

public interface UserRepository
        extends JpaRepository<User, Integer> {

    Optional<User> findByUserName(String userName);
}