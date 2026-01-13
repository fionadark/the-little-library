package thelittlelibrary.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase configuration class.
 * Initializes Firebase Admin SDK for server-side operations including
 * authentication and Firestore database access.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-key-path:}")
    private String serviceAccountKeyPath;

    @Value("${firebase.project-id}")
    private String projectId;

    /**
     * Initialize Firebase App on application startup.
     * Uses service account credentials for authentication.
     */
    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options;

            if (serviceAccountKeyPath != null && !serviceAccountKeyPath.isEmpty()) {
                // Initialize with service account key file
                FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);
                
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();
                
                logger.info("Firebase initialized with service account key from: {}", serviceAccountKeyPath);
            } else {
                // Initialize with application default credentials (for production/cloud environments)
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId(projectId)
                        .build();
                
                logger.info("Firebase initialized with application default credentials");
            }

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase App initialized successfully for project: {}", projectId);
            }

        } catch (IOException e) {
            logger.error("Failed to initialize Firebase", e);
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }

    /**
     * Provides FirebaseAuth instance for authentication operations.
     */
    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    /**
     * Provides Firestore instance for database operations.
     */
    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
