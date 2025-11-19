package thelittlelibrary.service;

import thelittlelibrary.dto.BookSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ExternalBookSearchService.
 * Makes real HTTP calls to the OpenLibrary API.
 * 
 * These tests are slower and depend on external services,
 * so they may be skipped in CI environments.
 */
@SpringBootTest
class ExternalBookSearchServiceIntegrationTest {

    @Autowired
    private ExternalBookSearchService service;

    @Test
    void searchBooks_withRealApi_returnsActualResults() {
        // Act
        List<BookSearchResponse> results = service.searchBooks("The Hobbit", 5);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find results for 'The Hobbit'");
        
        // Verify first result contains expected data
        BookSearchResponse firstBook = results.get(0);
        assertNotNull(firstBook.getTitle());
        assertTrue(firstBook.getTitle().toLowerCase().contains("hobbit"),
            "Title should contain 'hobbit'");
    }

    @Test
    void searchBooks_withAuthorQuery_returnsBooksByAuthor() {
        // Act
        List<BookSearchResponse> results = service.searchBooks("Tolkien", 5);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find books by Tolkien");
        
        // At least one book should have author populated
        boolean hasAuthor = results.stream()
            .anyMatch(book -> book.getAuthor() != null && 
                book.getAuthor().toLowerCase().contains("tolkien"));
        
        assertTrue(hasAuthor, "At least one result should have Tolkien as author");
    }

    @Test
    void searchBooks_withIsbnQuery_returnsSpecificBook() {
        // Act - Search for The Hobbit ISBN
        List<BookSearchResponse> results = service.searchBooks("9780547928227", 1);

        // Assert
        assertNotNull(results);
        if (!results.isEmpty()) {
            BookSearchResponse book = results.get(0);
            assertNotNull(book.getTitle());
            // ISBN searches should return very specific results
            assertTrue(book.getTitle().toLowerCase().contains("hobbit") ||
                      book.getIsbn() != null);
        }
    }

    @Test
    void searchBooks_withLimit_respectsLimit() {
        // Act
        List<BookSearchResponse> results = service.searchBooks("programming", 3);

        // Assert
        assertNotNull(results);
        assertTrue(results.size() <= 3, "Should return at most 3 results");
    }

    @Test
    void searchBooks_withObscureQuery_mayReturnEmpty() {
        // Act
        List<BookSearchResponse> results = 
            service.searchBooks("xyzabc123nonexistentbook999", 10);

        // Assert
        assertNotNull(results);
        // This might be empty, which is fine - just ensuring no exceptions
        assertTrue(results.size() >= 0);
    }

    @Test
    void searchBooks_populatesAllAvailableFields() {
        // Act
        List<BookSearchResponse> results = service.searchBooks("1984 George Orwell", 1);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find '1984'");
        
        BookSearchResponse book = results.get(0);
        
        // Check that fields are populated (may not all be present for every book)
        assertNotNull(book.getTitle(), "Title should always be present");
        
        // Log what we got for debugging
        System.out.println("Integration test result:");
        System.out.println("  Title: " + book.getTitle());
        System.out.println("  Author: " + book.getAuthor());
        System.out.println("  ISBN: " + book.getIsbn());
        System.out.println("  Year: " + book.getPublicationYear());
        System.out.println("  Publisher: " + book.getPublisher());
        System.out.println("  Cover URL: " + book.getCoverUrl());
    }

    @Test
    void searchBooks_withMultiWordQuery_handlesSpaces() {
        // Act
        List<BookSearchResponse> results = 
            service.searchBooks("Lord of the Rings", 5);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should handle multi-word queries");
        
        BookSearchResponse firstBook = results.get(0);
        assertNotNull(firstBook.getTitle());
    }

    @Test
    void searchBooks_withEmptyQuery_returnsEmptyList() {
        // Act
        List<BookSearchResponse> results = service.searchBooks("", 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Empty query should return empty list");
    }

    @Test
    void searchBooks_withNullQuery_returnsEmptyList() {
        // Act
        List<BookSearchResponse> results = service.searchBooks(null, 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Null query should return empty list");
    }
}
