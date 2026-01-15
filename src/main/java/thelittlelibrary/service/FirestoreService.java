package thelittlelibrary.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service for Firestore database operations.
 * Provides methods for CRUD operations on Firestore collections.
 */
@Service
public class FirestoreService {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreService.class);

    @Autowired
    private Firestore firestore;

    /**
     * Creates or updates a document in a collection.
     * 
     * @param collection Collection name
     * @param documentId Document ID
     * @param data Document data
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public void saveDocument(String collection, String documentId, Map<String, Object> data) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<WriteResult> result = firestore.collection(collection)
                    .document(documentId)
                    .set(data);
            
            WriteResult writeResult = result.get();
            logger.info("Document saved in {}/{} at {}", collection, documentId, writeResult.getUpdateTime());
        } catch (Exception e) {
            logger.error("Failed to save document {}/{}: {}", collection, documentId, e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a new document with auto-generated ID.
     * 
     * @param collection Collection name
     * @param data Document data
     * @return The auto-generated document ID
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public String createDocument(String collection, Map<String, Object> data) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<DocumentReference> result = firestore.collection(collection).add(data);
            DocumentReference docRef = result.get();
            logger.info("Document created in {} with ID: {}", collection, docRef.getId());
            return docRef.getId();
        } catch (Exception e) {
            logger.error("Failed to create document in {}: {}", collection, e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a document by ID.
     * 
     * @param collection Collection name
     * @param documentId Document ID
     * @return Document data as Map, or null if not found
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public Map<String, Object> getDocument(String collection, String documentId) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<DocumentSnapshot> future = firestore.collection(collection)
                    .document(documentId)
                    .get();
            
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                logger.debug("Retrieved document {}/{}", collection, documentId);
                return document.getData();
            } else {
                logger.debug("Document {}/{} not found", collection, documentId);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to get document {}/{}: {}", collection, documentId, e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all documents from a collection.
     * 
     * @param collection Collection name
     * @return List of document data as Maps
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public List<Map<String, Object>> getAllDocuments(String collection) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(collection).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (DocumentSnapshot document : documents) {
                Map<String, Object> data = new HashMap<>(document.getData());
                data.put("id", document.getId()); // Include document ID
                results.add(data);
            }
            
            logger.debug("Retrieved {} documents from {}", results.size(), collection);
            return results;
        } catch (Exception e) {
            logger.error("Failed to get documents from {}: {}", collection, e.getMessage());
            throw e;
        }
    }

    /**
     * Queries documents based on a field value.
     * 
     * @param collection Collection name
     * @param field Field name to query
     * @param value Value to match
     * @return List of matching document data
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public List<Map<String, Object>> queryDocuments(String collection, String field, Object value) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(collection)
                    .whereEqualTo(field, value)
                    .get();
            
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Map<String, Object>> results = new ArrayList<>();
            
            for (DocumentSnapshot document : documents) {
                Map<String, Object> data = new HashMap<>(document.getData());
                data.put("id", document.getId()); // Include document ID
                results.add(data);
            }
            
            logger.debug("Query found {} documents in {} where {}={}", 
                    results.size(), collection, field, value);
            return results;
        } catch (Exception e) {
            logger.error("Failed to query documents in {}: {}", collection, e.getMessage());
            throw e;
        }
    }

    /**
     * Updates specific fields in a document.
     * 
     * @param collection Collection name
     * @param documentId Document ID
     * @param updates Map of field names to new values
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public void updateDocument(String collection, String documentId, Map<String, Object> updates) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<WriteResult> result = firestore.collection(collection)
                    .document(documentId)
                    .update(updates);
            
            WriteResult writeResult = result.get();
            logger.info("Document updated {}/{} at {}", collection, documentId, writeResult.getUpdateTime());
        } catch (Exception e) {
            logger.error("Failed to update document {}/{}: {}", collection, documentId, e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a document from a collection.
     * 
     * @param collection Collection name
     * @param documentId Document ID
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public void deleteDocument(String collection, String documentId) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<WriteResult> result = firestore.collection(collection)
                    .document(documentId)
                    .delete();
            
            result.get();
            logger.info("Document deleted {}/{}", collection, documentId);
        } catch (Exception e) {
            logger.error("Failed to delete document {}/{}: {}", collection, documentId, e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a document exists.
     * 
     * @param collection Collection name
     * @param documentId Document ID
     * @return true if document exists, false otherwise
     * @throws ExecutionException if the operation fails
     * @throws InterruptedException if the operation is interrupted
     */
    public boolean documentExists(String collection, String documentId) 
            throws ExecutionException, InterruptedException {
        try {
            ApiFuture<DocumentSnapshot> future = firestore.collection(collection)
                    .document(documentId)
                    .get();
            
            return future.get().exists();
        } catch (Exception e) {
            logger.error("Failed to check if document exists {}/{}: {}", collection, documentId, e.getMessage());
            throw e;
        }
    }
}
