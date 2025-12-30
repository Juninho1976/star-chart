package com.example.starchart.security;

import com.example.starchart.domain.Role;
import java.util.UUID;

public record JwtPrincipal(UUID userId, String email, Role role) {}