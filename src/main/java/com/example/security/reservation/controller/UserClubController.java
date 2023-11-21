package com.example.security.reservation.controller;

import com.example.security.reservation.service.UserClubService;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import com.example.security.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-club")
@RequiredArgsConstructor
public class UserClubController {


    private final UserClubService userClubService;
    private final UserRepository userRepository;

    @PostMapping("/follow")
    public ResponseEntity<String> followClub(@AuthenticationPrincipal UserDetails authentication,
                                             @RequestParam Integer clubId) {
        User user = userRepository.findByEmail(authentication.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userClubService.followClub(user, clubId);
        return ResponseEntity.ok("User now follows the club.");
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollowClub(@AuthenticationPrincipal UserDetails authentication, @RequestParam Integer clubId) {
        User user = userRepository.findByEmail(authentication.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userClubService.unfollowClub(user, clubId);
        return ResponseEntity.ok("You have unfollowed the club.");
    }
}
