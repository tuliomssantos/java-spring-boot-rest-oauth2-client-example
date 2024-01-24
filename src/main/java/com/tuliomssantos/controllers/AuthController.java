package com.tuliomssantos.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tuliomssantos.models.UserModel;
import com.tuliomssantos.services.UserService;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getUser(@AuthenticationPrincipal OAuth2User principal) {

        var email = principal.getAttribute("email").toString();

        Optional<UserModel> userModel = userService.findByEmail(email);

        if (userModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userModel.get();

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(Collections.singletonMap("message", "Logout successful"));
    }
}
