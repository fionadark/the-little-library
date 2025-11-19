package thelittlelibrary.controller;

import thelittlelibrary.dto.BookSearchResponse;
import thelittlelibrary.service.ExternalBookSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookSearchController.
 * Tests controller methods in isolation using mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class BookSearchControllerTest {

    @Mock
    private ExternalBookSearchService bookSearchService;

    @InjectMocks
    private BookSearchController controller;

    private BookSearchResponse sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new BookSearchResponse();
        sampleBook.setTitle("The Hobbit");
        sampleBook.setAuthor("J.R.R. Tolkien");
        sampleBook.setIsbn("9780547928227");
        sampleBook.setPublicationYear(1937);
        sampleBook.setPublisher("Houghton Mifflin");
    }

    // searchBooks() tests

    @Test
    void searchBooks_withValidQuery_returnsOkWithResults() {
        // Arrange
        List<BookSearchResponse> mockResults = List.of(sampleBook);
        when(bookSearchService.searchBooks("hobbit", 10))
            .thenReturn(mockResults);

        // Act
        ResponseEntity<List<BookSearchResponse>> response = 
            controller.searchBooks("hobbit", 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("The Hobbit", response.getBody().get(0).getTitle());
        verify(bookSearchService).searchBooks("hobbit", 10);
    }

    @Test
    void searchBooks_withEmptyResults_returnsOkWithEmptyList() {
        // Arrange
        when(bookSearchService.searchBooks(anyString(), anyInt()))
            .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<BookSearchResponse>> response = 
            controller.searchBooks("nonexistentbook123", 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void searchBooks_withNullQuery_returnsBadRequest() {
        // Act
        ResponseEntity<List<BookSearchResponse>> response = 
            controller.searchBooks(null, 10);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookSearchService, never()).searchBooks(anyString(), anyInt());
    }

    @Test
    void searchBooks_withEmptyQuery_returnsBadRequest() {
        // Act
        ResponseEntity<List<BookSearchResponse>> response = 
            controller.searchBooks("", 10);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookSearchService, never()).searchBooks(anyString(), anyInt());
    }

    @Test
    void searchBooks_withWhitespaceQuery_returnsBadRequest() {
        // Act
        ResponseEntity<List<BookSearchResponse>> response = 
            controller.searchBooks("   ", 10);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookSearchService, never()).searchBooks(anyString(), anyInt());
    }

    @Test
    void searchBooks_withCustomLimit_passesLimitToService() {
        // Arrange
        when(bookSearchService.searchBooks("test", 5))
            .thenReturn(Collections.emptyList());

        // Act
        controller.searchBooks("test", 5);

        // Assert
        verify(bookSearchService).searchBooks("test", 5);
    }

    // searchByIsbn() tests

    @Test
    void searchByIsbn_withValidIsbn_returnsOkWithBook() {
        // Arrange
        List<BookSearchResponse> mockResults = List.of(sampleBook);
        when(bookSearchService.searchBooks("9780547928227", 1))
            .thenReturn(mockResults);

        // Act
        ResponseEntity<BookSearchResponse> response = 
            controller.searchByIsbn("9780547928227");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The Hobbit", response.getBody().getTitle());
        assertEquals("9780547928227", response.getBody().getIsbn());
        verify(bookSearchService).searchBooks("9780547928227", 1);
    }

    @Test
    void searchByIsbn_withNoResults_returnsNotFound() {
        // Arrange
        when(bookSearchService.searchBooks(anyString(), anyInt()))
            .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<BookSearchResponse> response = 
            controller.searchByIsbn("0000000000000");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void searchByIsbn_withNullIsbn_returnsBadRequest() {
        // Act
        ResponseEntity<BookSearchResponse> response = 
            controller.searchByIsbn(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookSearchService, never()).searchBooks(anyString(), anyInt());
    }

    @Test
    void searchByIsbn_withEmptyIsbn_returnsBadRequest() {
        // Act
        ResponseEntity<BookSearchResponse> response = 
            controller.searchByIsbn("");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookSearchService, never()).searchBooks(anyString(), anyInt());
    }

    @Test
    void searchByIsbn_withWhitespaceIsbn_returnsBadRequest() {
        // Act
        ResponseEntity<BookSearchResponse> response = 
            controller.searchByIsbn("   ");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookSearchService, never()).searchBooks(anyString(), anyInt());
    }

    // healthCheck() tests

    @Test
    void healthCheck_whenServiceIsUp_returnsOkWithUpStatus() {
        // Arrange
        when(bookSearchService.searchBooks("test", 1))
            .thenReturn(List.of(sampleBook));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("OpenLibrary API", response.getBody().get("service"));
        assertEquals("Service is operational", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
        verify(bookSearchService).searchBooks("test", 1);
    }

    @Test
    void healthCheck_whenServiceThrowsException_returnsServiceUnavailable() {
        // Arrange
        when(bookSearchService.searchBooks("test", 1))
            .thenThrow(new RuntimeException("API connection failed"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DOWN", response.getBody().get("status"));
        assertEquals("OpenLibrary API", response.getBody().get("service"));
        assertTrue(response.getBody().get("message").toString().contains("API connection failed"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void healthCheck_whenServiceReturnsEmpty_stillReturnsUp() {
        // Arrange
        when(bookSearchService.searchBooks("test", 1))
            .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
    }
}
