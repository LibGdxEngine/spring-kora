package com.example.security.auth;

import com.example.security.auth.dto.*;
import com.example.security.user.UserRepository;
import com.example.security.utils.exceptions.UserAlreadyRegisteredException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @PostMapping("/pre-register")
    public ResponseEntity<PreRegistrationResponse> preRegister(@RequestBody PreRegisterRequest preRegisterRequest) {
        return ResponseEntity.ok(authenticationService.preRegister(preRegisterRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        var userExist = userRepository.findByEmail(registerRequest.email()).isPresent();
        if (userExist) {
            throw new UserAlreadyRegisteredException("User with this email is already registered, try sign in");
        }
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        var userExist = userRepository.findByEmail(registerRequest.email()).isPresent();
        if (userExist) {
            throw new UserAlreadyRegisteredException("User with this email is already registered, try sign in");
        }
        return ResponseEntity.ok(authenticationService.registerAdmin(registerRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }


}
