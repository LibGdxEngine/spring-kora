package com.example.security.auth;

import com.example.security.auth.dto.*;
import com.example.security.config.services.JwtService;
import com.example.security.reservation.entity.Club;
import com.example.security.reservation.entity.UserClub;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import com.example.security.utils.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    public PreRegistrationResponse preRegister(PreRegisterRequest preRegisterRequest) {
        var user = User.builder()
                .email(preRegisterRequest.email())
                .password(passwordEncoder.encode(preRegisterRequest.password()))
                .role(Role.USER)
                .build();
        var jwtToken = jwtService.generateToken(Map.of("pre-signup", true, "password", user.getPassword()), user);
        //send mail to the user with jwt
        try {
            emailService.sendHtmlEmail(preRegisterRequest.email(), "Hello world",
                    "<h1>Hello from myapp</h1>" +
                            "<p>It can contain <strong>HTML</strong> content.</p>");
            return new PreRegistrationResponse(jwtToken, "Mail sent successfully", 200);
        } catch (MessagingException e) {
            return new PreRegistrationResponse(null, "Can't send mail, try again later", 500);
        }
    }

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        var user = User.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(Map.of("register", true), user);
        return new AuthenticationResponse(jwtToken, user.getEmail(), user.getRole().toString(), null);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.email(),
                        authenticationRequest.password()
                )
        );
        var user = userRepository.findByEmail(authenticationRequest.email()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken,
                user.getEmail(),
                user.getRole().toString(),
                user.getFollowedClubs().stream().map(UserClub::getClub).map(Club::getId).toList()
        );
    }


    public AuthenticationResponse registerAdmin(RegisterRequest registerRequest) {
        var user = User.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(Role.ADMIN)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(Map.of("registerAdmin", true), user);
        return new AuthenticationResponse(jwtToken, user.getEmail(), user.getRole().toString(), null);
    }
}
