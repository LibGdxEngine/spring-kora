package com.example.security.config.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_API_BASE_URL = "https://people.googleapis.com/v1/people/me";


    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientID;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectURI;

    public String getAccessToken(String code, String state) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("state", state);
        body.add("client_id", "YourClientID");
        body.add("client_secret", "YourClientSecret");
        body.add("redirect_uri", "YourRedirectURI");
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, requestEntity, String.class);

        // Handle the response, extract the access token, and handle errors as needed
        String responseBody = response.getBody();
//        TODO:Parse the JSON response to get the access token
        return responseBody;
    }

    public OAuth2User getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        RequestEntity<?> request = new RequestEntity<>(headers, HttpMethod.GET, UriComponentsBuilder
                .fromUriString(GOOGLE_API_BASE_URL)
                .build()
                .toUri());

        ResponseEntity<OAuth2User> response = restTemplate.exchange(request, OAuth2User.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve Google user data");
        }
    }
}
