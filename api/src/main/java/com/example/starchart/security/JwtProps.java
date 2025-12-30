package com.example.starchart.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProps(String secret, String issuer, int expMinutes) {}