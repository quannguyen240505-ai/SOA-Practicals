package com.example.bth2jwt.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.bth2jwt.dto.LoginRequest;
import com.example.bth2jwt.dto.LoginResponse;
import com.example.bth2jwt.entity.User;
import com.example.bth2jwt.repository.UserRepository;
import com.example.bth2jwt.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
public class AuthController {

	private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        Optional<User> optionalUser =
                userRepository.findByUserName(
                        request.getUserName()
                );

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Sai tai khoan hoac mat khau");
        }

        String decodedPassword;

        try {
            byte[] decodedBytes =
                    Base64.getDecoder()
                            .decode(request.getPassword());

            decodedPassword =
                    new String(
                            decodedBytes,
                            StandardCharsets.UTF_8
                    );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body("Password khong dung Base64");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(
                decodedPassword,
                user.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Sai tai khoan hoac mat khau");
        }

        String token =
                jwtService.generateToken(
                        user.getUserName()
                );

        user.setToken(token);
        userRepository.save(user);

        return ResponseEntity.ok(
                new LoginResponse(
                        "Dang nhap thanh cong",
                        token
                )
        );
    }
    
    @GetMapping("/auth")
    public ResponseEntity<?> auth(
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of(
                        "valid", true,
                        "userName",
                        authentication.getName()
                )
        );
    }
    
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {

        return ResponseEntity.ok("Hello World");
    }
}