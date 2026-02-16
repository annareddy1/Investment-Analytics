package com.marketlens.controller;

import com.marketlens.security.JwtUserExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Example controller demonstrating JWT authentication and authorization
 *
 * Shows multiple ways to access authenticated user information:
 * 1. Using JwtUserExtractor utility (recommended)
 * 2. Using @AuthenticationPrincipal annotation
 * 3. Using Principal parameter
 * 4. Method-level security with @PreAuthorize and @Secured
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final JwtUserExtractor jwtUserExtractor;

    /**
     * Get current user profile
     * Accessible to any authenticated user
     *
     * Example request:
     * GET /api/user/profile
     * Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        log.info("Fetching user profile for authenticated user");

        Map<String, Object> profile = new HashMap<>();

        // Extract user information using JwtUserExtractor
        profile.put("userId", jwtUserExtractor.getUserId());
        profile.put("email", jwtUserExtractor.getUserEmail());
        profile.put("username", jwtUserExtractor.getUsername());
        profile.put("roles", jwtUserExtractor.getUserRoles().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        profile.put("isAuthenticated", jwtUserExtractor.isAuthenticated());

        // Include all JWT claims (for debugging - remove in production)
        profile.put("allClaims", jwtUserExtractor.getAllClaims());

        log.debug("User profile: {}", profile);

        return ResponseEntity.ok(profile);
    }

    /**
     * Alternative way: Using Principal parameter
     * Spring automatically injects the authenticated principal
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Principal principal) {
        Map<String, Object> info = new HashMap<>();

        if (principal instanceof JwtAuthenticationToken jwtAuth) {
            info.put("userId", jwtAuth.getToken().getSubject());
            info.put("tokenValue", jwtAuth.getToken().getTokenValue()); // Full JWT string
            info.put("issuedAt", jwtAuth.getToken().getIssuedAt());
            info.put("expiresAt", jwtAuth.getToken().getExpiresAt());
            info.put("authorities", jwtAuth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        }

        return ResponseEntity.ok(info);
    }

    /**
     * Role-based access control example using @PreAuthorize
     * Only users with ROLE_USER can access this endpoint
     *
     * @PreAuthorize uses SpEL (Spring Expression Language)
     */
    @GetMapping("/user-only")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> userOnlyEndpoint() {
        return ResponseEntity.ok(Map.of(
            "message", "This endpoint is accessible to ROLE_USER",
            "userId", jwtUserExtractor.getUserId()
        ));
    }

    /**
     * Admin-only endpoint using @Secured annotation
     * Only users with ROLE_ADMIN can access this endpoint
     *
     * Returns 403 Forbidden if user doesn't have ROLE_ADMIN
     */
    @GetMapping("/admin-only")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Map<String, String>> adminOnlyEndpoint() {
        return ResponseEntity.ok(Map.of(
            "message", "This endpoint is accessible to ROLE_ADMIN only",
            "adminId", jwtUserExtractor.getUserId()
        ));
    }

    /**
     * Multiple roles allowed using @PreAuthorize with OR condition
     */
    @GetMapping("/admin-or-user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> multiRoleEndpoint() {
        boolean isAdmin = jwtUserExtractor.hasRole("ADMIN");

        return ResponseEntity.ok(Map.of(
            "message", "Accessible to ROLE_USER or ROLE_ADMIN",
            "userId", jwtUserExtractor.getUserId(),
            "isAdmin", String.valueOf(isAdmin)
        ));
    }

    /**
     * Example: Get user-specific data
     * This demonstrates how you would use userId in real business logic
     */
    @GetMapping("/my-analyses")
    public ResponseEntity<Map<String, Object>> getMyAnalyses() {
        String userId = jwtUserExtractor.getUserId();

        log.info("Fetching analyses for user: {}", userId);

        // In real implementation, you would:
        // 1. Query database for analyses created by this userId
        // 2. Return user-specific data
        // Example: analysisRepository.findByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("message", "This would return analyses created by user: " + userId);
        response.put("analyses", "[]"); // Placeholder for actual data

        return ResponseEntity.ok(response);
    }
}
