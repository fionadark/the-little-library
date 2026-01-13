package thelittlelibrary.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Firebase Authentication operations.
 * Handles user verification, token validation, and user management.
 */
@Service
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    @Autowired
    private FirebaseAuth firebaseAuth;

    /**
     * Verifies a Firebase ID token sent from the client.
     * 
     * @param idToken The Firebase ID token to verify
     * @return FirebaseToken containing user information
     * @throws FirebaseAuthException if token is invalid
     */
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            logger.debug("Token verified for user: {}", decodedToken.getUid());
            return decodedToken;
        } catch (FirebaseAuthException e) {
            logger.error("Failed to verify ID token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Gets user information by user ID.
     * 
     * @param uid The Firebase user ID
     * @return UserRecord containing user details
     * @throws FirebaseAuthException if user not found
     */
    public UserRecord getUserById(String uid) throws FirebaseAuthException {
        try {
            UserRecord userRecord = firebaseAuth.getUser(uid);
            logger.debug("Retrieved user: {}", uid);
            return userRecord;
        } catch (FirebaseAuthException e) {
            logger.error("Failed to get user {}: {}", uid, e.getMessage());
            throw e;
        }
    }

    /**
     * Gets user information by email.
     * 
     * @param email The user's email address
     * @return UserRecord containing user details
     * @throws FirebaseAuthException if user not found
     */
    public UserRecord getUserByEmail(String email) throws FirebaseAuthException {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            logger.debug("Retrieved user by email: {}", email);
            return userRecord;
        } catch (FirebaseAuthException e) {
            logger.error("Failed to get user by email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a new user with email and password.
     * 
     * @param email User's email address
     * @param password User's password
     * @return UserRecord of the created user
     * @throws FirebaseAuthException if user creation fails
     */
    public UserRecord createUser(String email, String password) throws FirebaseAuthException {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false);

            UserRecord userRecord = firebaseAuth.createUser(request);
            logger.info("Successfully created new user: {}", userRecord.getUid());
            return userRecord;
        } catch (FirebaseAuthException e) {
            logger.error("Failed to create user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Updates an existing user.
     * 
     * @param uid User ID
     * @param email New email (optional)
     * @param password New password (optional)
     * @return Updated UserRecord
     * @throws FirebaseAuthException if update fails
     */
    public UserRecord updateUser(String uid, String email, String password) throws FirebaseAuthException {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid);
            
            if (email != null && !email.isEmpty()) {
                request.setEmail(email);
            }
            if (password != null && !password.isEmpty()) {
                request.setPassword(password);
            }

            UserRecord userRecord = firebaseAuth.updateUser(request);
            logger.info("Successfully updated user: {}", uid);
            return userRecord;
        } catch (FirebaseAuthException e) {
            logger.error("Failed to update user {}: {}", uid, e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a user account.
     * 
     * @param uid User ID to delete
     * @throws FirebaseAuthException if deletion fails
     */
    public void deleteUser(String uid) throws FirebaseAuthException {
        try {
            firebaseAuth.deleteUser(uid);
            logger.info("Successfully deleted user: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Failed to delete user {}: {}", uid, e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts user ID from the authorization header.
     * 
     * @param authorizationHeader The Authorization header value (Bearer token)
     * @return User ID if token is valid, null otherwise
     */
    public String getUserIdFromAuthHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7);
        try {
            FirebaseToken decodedToken = verifyIdToken(token);
            return decodedToken.getUid();
        } catch (FirebaseAuthException e) {
            logger.warn("Invalid token in authorization header: {}", e.getMessage());
            return null;
        }
    }
}
