package com.marketlens.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class to extract authenticated user information from JWT tokens
 *
 * Provides convenient methods to:
 * - Get user ID (from "sub" claim)
 * - Get username/email (from custom claims)
 * - Get user roles
 * - Access any JWT claim
 *
 * Usage in controllers:
 * <pre>
 *   @Autowired
 *   private JwtUserExtractor jwtUserExtractor;
 *
 *   public ResponseEntity<?> myEndpoint() {
 *       String userId = jwtUserExtractor.getUserId();
 *       // Use userId for business logic
 *   }
 * </pre>
 */
@Component
@Slf4j
public class JwtUserExtractor {

    /**
     * Get the authenticated user's ID from JWT "sub" claim
     *
     * The "sub" (subject) claim is the standard JWT claim for user identifier
     *
     * @return User ID, or null if not authenticated
     */
    public String getUserId() {
        return getJwt()
            .map(Jwt::getSubject)
            .orElse(null);
    }

    /**
     * Get user email from JWT claims
     *
     * Checks common email claim names: "email", "preferred_username", "upn"
     *
     * @return User email, or null if not found
     */
    public String getUserEmail() {
        return getJwt()
            .flatMap(jwt -> {
                // Try common email claim names
                String email = jwt.getClaimAsString("email");
                if (email != null) return Optional.of(email);

                email = jwt.getClaimAsString("preferred_username");
                if (email != null) return Optional.of(email);

                email = jwt.getClaimAsString("upn"); // User Principal Name (Azure AD)
                return Optional.ofNullable(email);
            })
            .orElse(null);
    }

    /**
     * Get username from JWT claims
     *
     * Tries: "preferred_username", "username", "name"
     *
     * @return Username, or null if not found
     */
    public String getUsername() {
        return getJwt()
            .flatMap(jwt -> {
                String username = jwt.getClaimAsString("preferred_username");
                if (username != null) return Optional.of(username);

                username = jwt.getClaimAsString("username");
                if (username != null) return Optional.of(username);

                username = jwt.getClaimAsString("name");
                return Optional.ofNullable(username);
            })
            .orElse(null);
    }

    /**
     * Get all user roles/authorities
     *
     * @return Collection of granted authorities (roles)
     */
    public Collection<? extends GrantedAuthority> getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        }
        return Collections.emptyList();
    }

    /**
     * Check if user has a specific role
     *
     * @param role Role name (with or without ROLE_ prefix)
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getUserRoles().stream()
            .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
    }

    /**
     * Get a custom claim from JWT
     *
     * @param claimName Name of the claim
     * @return Claim value as String, or null if not found
     */
    public String getClaim(String claimName) {
        return getJwt()
            .map(jwt -> jwt.getClaimAsString(claimName))
            .orElse(null);
    }

    /**
     * Get all JWT claims as a map
     *
     * @return Map of all claims, or empty map if not authenticated
     */
    public Map<String, Object> getAllClaims() {
        return getJwt()
            .map(Jwt::getClaims)
            .orElse(Collections.emptyMap());
    }

    /**
     * Get the raw JWT token object
     *
     * @return Optional containing JWT if authenticated, empty otherwise
     */
    public Optional<Jwt> getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }

        return Optional.empty();
    }

    /**
     * Check if the current request is authenticated
     *
     * @return true if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.isAuthenticated() &&
               !(authentication.getPrincipal().equals("anonymousUser"));
    }

    /**
     * Get the full Authentication object
     *
     * @return Current authentication, or null
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
