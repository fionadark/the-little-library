package thelittlelibrary.controller;

import com.google.firebase.auth.FirebaseToken;
import thelittlelibrary.service.FirebaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Firebase authentication endpoints.
 * Handles token verification and user authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    /**
     * Verifies the Firebase authentication token from the Authorization header.
     * This endpoint can be used to test if authentication is working.
     * 
     * @param authHeader Authorization header containing "Bearer {token}"
     * @return User information if token is valid, error otherwise
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check if Authorization header is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Missing or invalid Authorization header");
            response.put("hint", "Include 'Authorization: Bearer {your-token}' header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // Extract token from "Bearer {token}"
            String token = authHeader.substring(7);
            
            // Verify the token with Firebase
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(token);
            
            // Token is valid - return user info
            response.put("success", true);
            response.put("message", "Token verified successfully");
            response.put("user", Map.of(
                "uid", decodedToken.getUid(),
                "email", decodedToken.getEmail() != null ? decodedToken.getEmail() : "N/A",
                "name", decodedToken.getName() != null ? decodedToken.getName() : "N/A",
                "emailVerified", decodedToken.isEmailVerified()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token verification failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Gets the currently authenticated user's information.
     * Requires valid authentication token.
     * 
     * @param authHeader Authorization header containing "Bearer {token}"
     * @return Current user's profile information
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String token = authHeader.substring(7);
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(token);
            
            // Get additional user info from Firebase
            var userRecord = firebaseAuthService.getUserById(decodedToken.getUid());
            
            response.put("success", true);
            response.put("user", Map.of(
                "uid", userRecord.getUid(),
                "email", userRecord.getEmail() != null ? userRecord.getEmail() : "N/A",
                "displayName", userRecord.getDisplayName() != null ? userRecord.getDisplayName() : "N/A",
                "emailVerified", userRecord.isEmailVerified(),
                "creationTime", userRecord.getUserMetadata().getCreationTimestamp()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get user information");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
