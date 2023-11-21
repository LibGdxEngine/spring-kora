package com.example.security.config.services;

import com.example.security.config.dto.OAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GitHubOAuth2Service {
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";

    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientID;
    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String redirectURI;

    public String getAccessToken(String code, String state) {
        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Set up the request parameters
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build the request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();


        body.add("client_id", clientID);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectURI);
        body.add("state", state);

        // Create the HTTP entity with the headers and body
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Make the POST request to exchange code for a token
        ResponseEntity<String> responseEntity = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, requestEntity, String.class);

        // Parse the JSON response to get the access token
        String responseBody = responseEntity.getBody();
        System.out.println(responseBody);
        String accessToken = null;

        // Extract the access token from the response body
        if (responseBody != null) {
            String[] parts = responseBody.split("&");
            for (String part : parts) {
                if (part.startsWith("access_token=")) {
                    accessToken = part.substring("access_token=".length());
                    break;
                }
            }
        }

        return accessToken;
    }

    public OAuth2User getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        RequestEntity<?> request = new RequestEntity<>(headers, HttpMethod.GET, UriComponentsBuilder
                .fromUriString(GITHUB_API_BASE_URL + "/user")
                .build()
                .toUri());

        ResponseEntity<OAuth2User> response = restTemplate.exchange(request, OAuth2User.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve GitHub user data");
        }
    }
}

