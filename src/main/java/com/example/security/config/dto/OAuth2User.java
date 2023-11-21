package com.example.security.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuth2User(Long id,
                         String email,
                         String login,
                         @JsonProperty("avatar_url")
                         String avatarUrl)
{

}
