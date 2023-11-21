package com.example.security.auth;

import com.example.security.config.services.GitHubOAuth2Service;
import com.example.security.config.services.GoogleOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2/login")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final AuthenticationService authenticationService;
    private final GitHubOAuth2Service gitHubOAuth2Service;
    private final GoogleOAuth2Service googleOAuth2Service;

    @GetMapping("/github")
    public String getGithubOAuth2UserDetails(@RequestParam String code , @RequestParam String state) {
        String token = gitHubOAuth2Service.getAccessToken(code, state);
        var result = gitHubOAuth2Service.getUserInfo(token);
        return "asd" + " : " + result.toString();
    }

    @GetMapping("/google")
    public String getGoogleOAuth2UserDetails(@RequestParam String code , @RequestParam String state) {
        System.out.println(code);
        System.out.println(state);
        String token = googleOAuth2Service.getAccessToken(code, state);
        System.out.println(token);
        var result = googleOAuth2Service.getUserInfo(token);
        return "ewq" + " : " + result.toString();
    }

//    @GetMapping("/login")
//    public ResponseEntity<AuthenticationResponse> preRegister(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
//                                                              @AuthenticationPrincipal OAuth2User oauth2User) {
//        authorizedClient.getAccessToken();
//        var registerRequest = new RegisterRequest(oauth2User.getAttribute("name"),
//                oauth2User.getAttribute("name"),
//                oauth2User.getName(),
//                oauth2User.getName());
//        return ResponseEntity.ok(authenticationService.register(registerRequest));
//    }
}
