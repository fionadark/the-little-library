package thelittlelibrary.controller;

import thelittlelibrary.dto.BookSearchResponse;
import thelittlelibrary.service.ExternalBookSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified REST controller for book search operations.
 * Provides basic search functionality using OpenLibrary API.
 */
@RestController
@RequestMapping("/search")
public class BookSearchController {

    @Autowired
    private ExternalBookSearchService bookSearchService;

    /**
     * Search for books by query.
     * 
     * @param query Search query (title, author, or general search)
     * @param limit Maximum number of results (default: 10)
     * @return List of books matching the query
     */
    @GetMapping("/books")
    public ResponseEntity<List<BookSearchResponse>> searchBooks(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<BookSearchResponse> results = bookSearchService.searchBooks(query, limit);
        return ResponseEntity.ok(results);
    }

    /**
     * Search for a book by ISBN.
     * 
     * @param isbn ISBN to search for
     * @return Book details if found
     */
    @GetMapping("/books/isbn/{isbn}")
    public ResponseEntity<BookSearchResponse> searchByIsbn(@PathVariable String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<BookSearchResponse> results = bookSearchService.searchBooks(isbn, 1);
        
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(results.get(0));
    }

    /**
     * Health check endpoint to verify OpenLibrary API is reachable.
     * 
     * @return Health status information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Attempt a simple search to verify API is working
            List<BookSearchResponse> testResults = bookSearchService.searchBooks("test", 1);
            
            health.put("status", "UP");
            health.put("service", "OpenLibrary API");
            health.put("message", "Service is operational");
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("service", "OpenLibrary API");
            health.put("message", "Service unavailable: " + e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}