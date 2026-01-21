package thelittlelibrary.service;

import thelittlelibrary.dto.BookSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExternalBookSearchService.
 * Tests service methods in isolation using mocked RestTemplate.
 */
@ExtendWith(MockitoExtension.class)
class ExternalBookSearchServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ExternalBookSearchService service;

    private Map<String, Object> mockOpenLibraryResponse;

    @BeforeEach
    void setUp() {
        service = new ExternalBookSearchService();
        
        // Inject mock RestTemplate and base URLs
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "openLibraryBaseUrl", "https://openlibrary.org");
        ReflectionTestUtils.setField(service, "openLibraryCoverBaseUrl", "https://covers.openlibrary.org");

        // Create sample OpenLibrary API response
        mockOpenLibraryResponse = createMockOpenLibraryResponse();
    }

    private Map<String, Object> createMockOpenLibraryResponse() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> docs = new ArrayList<>();

        Map<String, Object> book = new HashMap<>();
        book.put("title", "The Hobbit");
        book.put("author_name", List.of("J.R.R. Tolkien", "Douglas A. Anderson"));
        book.put("isbn", List.of("9780547928227", "0547928220"));
        book.put("first_publish_year", 1937);
        book.put("publisher", List.of("Houghton Mifflin", "HarperCollins"));
        book.put("cover_i", 12345);

        docs.add(book);
        response.put("docs", docs);
        
        return response;
    }

    // searchBooks() main functionality tests

    @Test
    void searchBooks_withValidQuery_returnsBookList() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("hobbit", 10);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        
        BookSearchResponse book = results.get(0);
        assertEquals("The Hobbit", book.getTitle());
        assertEquals("J.R.R. Tolkien", book.getAuthor());
        assertEquals("9780547928227", book.getIsbn());
        assertEquals(1937, book.getPublicationYear());
        assertEquals("Houghton Mifflin", book.getPublisher());
        assertEquals("https://covers.openlibrary.org/b/id/12345-M.jpg", book.getCoverUrl());
    }

    @Test
    void searchBooks_withMultipleResults_returnsAllBooks() {
        // Arrange
        Map<String, Object> book2 = new HashMap<>();
        book2.put("title", "The Lord of the Rings");
        book2.put("author_name", List.of("J.R.R. Tolkien"));
        book2.put("isbn", List.of("9780544003415"));
        book2.put("first_publish_year", 1954);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> docs = (List<Map<String, Object>>) mockOpenLibraryResponse.get("docs");
        docs.add(book2);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("tolkien", 10);

        // Assert
        assertEquals(2, results.size());
        assertEquals("The Hobbit", results.get(0).getTitle());
        assertEquals("The Lord of the Rings", results.get(1).getTitle());
    }

    @Test
    void searchBooks_withNullQuery_returnsEmptyList() {
        // Act
        List<BookSearchResponse> results = service.searchBooks(null, 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(restTemplate, never()).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void searchBooks_withEmptyQuery_returnsEmptyList() {
        // Act
        List<BookSearchResponse> results = service.searchBooks("", 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(restTemplate, never()).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void searchBooks_withWhitespaceQuery_returnsEmptyList() {
        // Act
        List<BookSearchResponse> results = service.searchBooks("   ", 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(restTemplate, never()).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void searchBooks_whenApiReturnsNull_returnsEmptyList() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(null);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchBooks_whenApiReturnsEmptyDocs_returnsEmptyList() {
        // Arrange
        Map<String, Object> emptyResponse = new HashMap<>();
        emptyResponse.put("docs", Collections.emptyList());
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(emptyResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("nonexistent", 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchBooks_whenApiReturnsNoDocsKey_returnsEmptyList() {
        // Arrange
        Map<String, Object> invalidResponse = new HashMap<>();
        invalidResponse.put("error", "something went wrong");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(invalidResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchBooks_whenApiThrowsException_returnsEmptyList() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenThrow(new RestClientException("Connection failed"));

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 10);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchBooks_buildsCorrectUrl() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        service.searchBooks("the hobbit", 5);

        // Assert
        verify(restTemplate).getForObject(
            "https://openlibrary.org/search.json?q=the+hobbit&limit=5",
            Map.class
        );
    }

    // Field extraction tests

    @Test
    void searchBooks_withMissingAuthor_setsAuthorToNull() {
        // Arrange
        @SuppressWarnings("unchecked")
        Map<String, Object> book = ((List<Map<String, Object>>) mockOpenLibraryResponse.get("docs")).get(0);
        book.remove("author_name");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 1);

        // Assert
        assertNull(results.get(0).getAuthor());
    }

    @Test
    void searchBooks_withEmptyAuthorList_setsAuthorToNull() {
        // Arrange
        @SuppressWarnings("unchecked")
        Map<String, Object> book = ((List<Map<String, Object>>) mockOpenLibraryResponse.get("docs")).get(0);
        book.put("author_name", Collections.emptyList());
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 1);

        // Assert
        assertNull(results.get(0).getAuthor());
    }

    @Test
    void searchBooks_withMissingIsbn_setsIsbnToNull() {
        // Arrange
        @SuppressWarnings("unchecked")
        Map<String, Object> book = ((List<Map<String, Object>>) mockOpenLibraryResponse.get("docs")).get(0);
        book.remove("isbn");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 1);

        // Assert
        assertNull(results.get(0).getIsbn());
    }

    @Test
    void searchBooks_withMissingYear_setsYearToNull() {
        // Arrange
        @SuppressWarnings("unchecked")
        Map<String, Object> book = ((List<Map<String, Object>>) mockOpenLibraryResponse.get("docs")).get(0);
        book.remove("first_publish_year");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 1);

        // Assert
        assertNull(results.get(0).getPublicationYear());
    }

    @Test
    void searchBooks_withMissingPublisher_setsPublisherToNull() {
        // Arrange
        @SuppressWarnings("unchecked")
        Map<String, Object> book = ((List<Map<String, Object>>) mockOpenLibraryResponse.get("docs")).get(0);
        book.remove("publisher");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 1);

        // Assert
        assertNull(results.get(0).getPublisher());
    }

    @Test
    void searchBooks_withMissingCover_setsCoverUrlToNull() {
        // Arrange
        @SuppressWarnings("unchecked")
        Map<String, Object> book = ((List<Map<String, Object>>) mockOpenLibraryResponse.get("docs")).get(0);
        book.remove("cover_i");
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 1);

        // Assert
        assertNull(results.get(0).getCoverUrl());
    }

    @Test
    void searchBooks_withAllFieldsMissing_createsBookWithNullFields() {
        // Arrange
        Map<String, Object> minimalBook = new HashMap<>();
        minimalBook.put("title", "Minimal Book");
        
        Map<String, Object> minimalResponse = new HashMap<>();
        minimalResponse.put("docs", List.of(minimalBook));
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(minimalResponse);

        // Act
        List<BookSearchResponse> results = service.searchBooks("test", 1);

        // Assert
        assertEquals(1, results.size());
        BookSearchResponse book = results.get(0);
        assertEquals("Minimal Book", book.getTitle());
        assertNull(book.getAuthor());
        assertNull(book.getIsbn());
        assertNull(book.getPublicationYear());
        assertNull(book.getPublisher());
        assertNull(book.getCoverUrl());
    }
}
