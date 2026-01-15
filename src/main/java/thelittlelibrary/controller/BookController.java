package thelittlelibrary.controller;

import com.google.firebase.auth.FirebaseToken;
import thelittlelibrary.model.Book;
import thelittlelibrary.service.FirebaseAuthService;
import thelittlelibrary.service.FirestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing books in the user's personal library.
 * All endpoints require Firebase authentication.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private FirestoreService firestoreService;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    private static final String BOOKS_COLLECTION = "books";

    /**
     * Helper method to get user ID from Authorization header.
     */
    private String getUserIdFromAuth(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(token);
        return decodedToken.getUid();
    }

    /**
     * Add a book to the user's library.
     * 
     * @param authHeader Authorization header with Firebase token
     * @param book Book object to add
     * @return Added book with generated ID
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addBook(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody Book book) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user ID
            String userId = getUserIdFromAuth(authHeader);
            
            // Set the book owner
            book.setUserId(userId);
            
            // Save to Firestore
            String bookId = firestoreService.createDocument(BOOKS_COLLECTION, book.toFirestoreMap());
            book.setId(bookId);
            
            response.put("success", true);
            response.put("message", "Book added successfully");
            response.put("book", book);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all books for the authenticated user with optional search and filters.
     * 
     * @param authHeader Authorization header with Firebase token
     * @param searchQuery Optional search query to filter by title, author, or ISBN
     * @param status Optional filter by reading status
     * @param author Optional filter by author
     * @param location Optional filter by location
     * @param sortBy Optional sort field (title, author, dateAdded)
     * @param order Optional sort order (asc, desc)
     * @return List of user's books (filtered if parameters provided)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyBooks(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(value = "search", required = false) String searchQuery,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false, defaultValue = "asc") String order) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user ID
            String userId = getUserIdFromAuth(authHeader);
            
            // Query books by userId
            List<Map<String, Object>> bookMaps = firestoreService.queryDocuments(
                BOOKS_COLLECTION, "userId", userId);
            
            // Convert to Book objects
            List<Book> books = new ArrayList<>();
            for (Map<String, Object> bookMap : bookMaps) {
                String docId = (String) bookMap.get("id");
                if (docId == null) {
                    // If id not in data, we need to handle this differently
                    // For now, skip books without IDs
                    continue;
                }
                books.add(Book.fromFirestoreMap(docId, bookMap));
            }
            
            // Apply search filter if provided
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                books = filterBooksBySearch(books, searchQuery.trim());
            }
            
            // Apply status filter if provided
            if (status != null && !status.trim().isEmpty()) {
                books = filterBooksByStatus(books, status.trim());
            }
            
            // Apply author filter if provided
            if (author != null && !author.trim().isEmpty()) {
                books = filterBooksByAuthor(books, author.trim());
            }
            
            // Apply location filter if provided
            if (location != null && !location.trim().isEmpty()) {
                books = filterBooksByLocation(books, location.trim());
            }
            
            // Apply sorting if provided
            if (sortBy != null && !sortBy.trim().isEmpty()) {
                books = sortBooks(books, sortBy.trim(), order);
            }
            
            response.put("success", true);
            response.put("count", books.size());
            response.put("books", books);
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                response.put("searchQuery", searchQuery.trim());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve books: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Filters books by search query. Searches across title, author, ISBN, and publisher.
     * Case-insensitive search.
     * 
     * @param books List of books to filter
     * @param query Search query string
     * @return Filtered list of books matching the search query
     */
    private List<Book> filterBooksBySearch(List<Book> books, String query) {
        String lowerQuery = query.toLowerCase();
        
        return books.stream()
            .filter(book -> {
                // Search in title
                if (book.getTitle() != null && 
                    book.getTitle().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
                
                // Search in author
                if (book.getAuthor() != null && 
                    book.getAuthor().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
                
                // Search in ISBN (exact or partial match)
                if (book.getIsbn() != null && 
                    book.getIsbn().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
                
                // Search in publisher
                if (book.getPublisher() != null && 
                    book.getPublisher().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
                
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Filters books by reading status.
     * 
     * @param books List of books to filter
     * @param status Reading status to filter by
     * @return Filtered list of books
     */
    private List<Book> filterBooksByStatus(List<Book> books, String status) {
        return books.stream()
            .filter(book -> book.getReadingStatus() != null && 
                           book.getReadingStatus().equalsIgnoreCase(status))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Filters books by author.
     * Case-insensitive partial match.
     * 
     * @param books List of books to filter
     * @param author Author name to filter by
     * @return Filtered list of books
     */
    private List<Book> filterBooksByAuthor(List<Book> books, String author) {
        String lowerAuthor = author.toLowerCase();
        return books.stream()
            .filter(book -> book.getAuthor() != null && 
                           book.getAuthor().toLowerCase().contains(lowerAuthor))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Filters books by location.
     * Case-insensitive partial match.
     * 
     * @param books List of books to filter
     * @param location Location to filter by
     * @return Filtered list of books
     */
    private List<Book> filterBooksByLocation(List<Book> books, String location) {
        String lowerLocation = location.toLowerCase();
        return books.stream()
            .filter(book -> book.getLocation() != null && 
                           book.getLocation().toLowerCase().contains(lowerLocation))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Sorts books by the specified field and order.
     * 
     * @param books List of books to sort
     * @param sortBy Field to sort by (title, author, dateAdded)
     * @param order Sort order (asc or desc)
     * @return Sorted list of books
     */
    private List<Book> sortBooks(List<Book> books, String sortBy, String order) {
        java.util.Comparator<Book> comparator;
        
        switch (sortBy.toLowerCase()) {
            case "title":
                comparator = java.util.Comparator.comparing(
                    book -> book.getTitle() != null ? book.getTitle().toLowerCase() : "",
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                );
                break;
                
            case "author":
                comparator = java.util.Comparator.comparing(
                    book -> extractLastName(book.getAuthor()),
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                );
                break;
                
            case "dateadded":
                comparator = java.util.Comparator.comparing(
                    Book::getDateAdded,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                );
                break;
                
            default:
                // Default to title if invalid sortBy
                comparator = java.util.Comparator.comparing(
                    book -> book.getTitle() != null ? book.getTitle().toLowerCase() : "",
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                );
        }
        
        // Reverse if descending order
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        
        return books.stream()
            .sorted(comparator)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Extracts the last name from an author string for sorting purposes.
     * Handles various author name formats.
     * 
     * @param author Full author name
     * @return Last name in lowercase, or empty string if null
     */
    private String extractLastName(String author) {
        if (author == null || author.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = author.trim();
        String[] parts = trimmed.split("\\s+");
        
        // If multiple parts, assume last part is last name
        if (parts.length > 1) {
            return parts[parts.length - 1].toLowerCase();
        }
        
        // Single word name
        return trimmed.toLowerCase();
    }

    /**
     * Get a specific book by ID (must belong to authenticated user).
     * 
     * @param authHeader Authorization header with Firebase token
     * @param bookId Book document ID
     * @return Book details
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> getBook(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable String bookId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user ID
            String userId = getUserIdFromAuth(authHeader);
            
            // Get book from Firestore
            Map<String, Object> bookData = firestoreService.getDocument(BOOKS_COLLECTION, bookId);
            
            if (bookData == null) {
                response.put("success", false);
                response.put("message", "Book not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Verify book belongs to user
            String bookUserId = (String) bookData.get("userId");
            if (!userId.equals(bookUserId)) {
                response.put("success", false);
                response.put("message", "Unauthorized access to book");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            Book book = Book.fromFirestoreMap(bookId, bookData);
            
            response.put("success", true);
            response.put("book", book);
            
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update a book in the user's library.
     * 
     * @param authHeader Authorization header with Firebase token
     * @param bookId Book document ID
     * @param updates Map of fields to update
     * @return Success message
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> updateBook(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable String bookId,
            @RequestBody Map<String, Object> updates) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user ID
            String userId = getUserIdFromAuth(authHeader);
            
            // Verify book exists and belongs to user
            Map<String, Object> bookData = firestoreService.getDocument(BOOKS_COLLECTION, bookId);
            
            if (bookData == null) {
                response.put("success", false);
                response.put("message", "Book not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            String bookUserId = (String) bookData.get("userId");
            if (!userId.equals(bookUserId)) {
                response.put("success", false);
                response.put("message", "Unauthorized access to book");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Don't allow changing userId
            updates.remove("userId");
            
            // Update book
            firestoreService.updateDocument(BOOKS_COLLECTION, bookId, updates);
            
            response.put("success", true);
            response.put("message", "Book updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a book from the user's library.
     * 
     * @param authHeader Authorization header with Firebase token
     * @param bookId Book document ID
     * @return Success message
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> deleteBook(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable String bookId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user ID
            String userId = getUserIdFromAuth(authHeader);
            
            // Verify book exists and belongs to user
            Map<String, Object> bookData = firestoreService.getDocument(BOOKS_COLLECTION, bookId);
            
            if (bookData == null) {
                response.put("success", false);
                response.put("message", "Book not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            String bookUserId = (String) bookData.get("userId");
            if (!userId.equals(bookUserId)) {
                response.put("success", false);
                response.put("message", "Unauthorized access to book");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Delete book
            firestoreService.deleteDocument(BOOKS_COLLECTION, bookId);
            
            response.put("success", true);
            response.put("message", "Book deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete book: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
