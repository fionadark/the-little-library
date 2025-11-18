package thelittlelibrary.service;

import thelittlelibrary.dto.BookSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service to search books using OpenLibrary API.
 */
@Service
public class ExternalBookSearchService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalBookSearchService.class);

    @Value("${external.api.open-library.base-url:https://openlibrary.org}")
    private String openLibraryBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Searches for books using the OpenLibrary API and converts results to BookSearchResponse objects.
     * Returns an empty list if the query is invalid or the search fails.
     * 
     * @param query Search query string (e.g., "The Hobbit", "Tolkien", "isbn:0547928227")
     * @param limit Maximum number of results to return
     * @return List of matching books, or empty list if no results or error occurs
     */
    public List<BookSearchResponse> searchBooks(String query, int limit) {
        // Validate the query is not empty
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Build OpenLibrary search URL
            String url = String.format("%s/search.json?q=%s&limit=%d", 
                openLibraryBaseUrl, 
                query.replace(" ", "+"), 
                limit);

            logger.info("Searching OpenLibrary: {}", query);

            // Make the HTTP GET request
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            // Parse the response
            if (response == null || !response.containsKey("docs")) {
                return Collections.emptyList();
            }

            // Extract book details
            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");
            List<BookSearchResponse> books = new ArrayList<>();

            // Map the response to BookSearchResponse
            for (Map<String, Object> doc : docs) {
                // Create new BookSearchResponse object
                BookSearchResponse book = new BookSearchResponse();
                
                // Populate book details
                book.setTitle((String) doc.get("title"));
                book.setAuthor(extractFirstAuthor(doc.get("author_name")));
                book.setIsbn(extractFirstIsbn(doc.get("isbn")));
                book.setPublicationYear(extractYear(doc.get("first_publish_year")));
                book.setPublisher(extractFirstPublisher(doc.get("publisher")));
                
                // Add cover URL if available
                String coverKey = extractCoverKey(doc);
                if (coverKey != null) {
                    book.setCoverUrl(String.format("%s/b/id/%s-M.jpg", openLibraryBaseUrl, coverKey));
                }

                // Add book to the list
                books.add(book);
            }

            // Log and return the results
            logger.info("Found {} books for query: {}", books.size(), query);
            return books;

        } catch (Exception e) {
            // Log the error and return empty list
            logger.error("OpenLibrary search failed for '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Extracts the first author from the OpenLibrary author_name field.
     * @param authorName Author name object (typically a list of author names)
     * @return First author name, or null if none available
     */
    private String extractFirstAuthor(Object authorName) {
        if (authorName instanceof List && !((List<?>) authorName).isEmpty()) {
            return ((List<?>) authorName).get(0).toString();
        }
        return null;
    }

    /**
     * Extracts the first ISBN from the OpenLibrary isbn field.
     * @param isbn ISBN object (typically a list of ISBNs for different editions)
     * @return First ISBN, or null if none available
     */
    private String extractFirstIsbn(Object isbn) {
        if (isbn instanceof List && !((List<?>) isbn).isEmpty()) {
            return ((List<?>) isbn).get(0).toString();
        }
        return null;
    }

    /**
     * Extracts the publication year from the OpenLibrary first_publish_year field.
     * @param year Year object (typically a number)
     * @return Publication year as Integer, or null if not available
     */
    private Integer extractYear(Object year) {
        if (year instanceof Number) {
            return ((Number) year).intValue();
        }
        return null;
    }

    /**
     * Extracts the first publisher from the OpenLibrary publisher field.
     * @param publisher Publisher object (typically a list of publisher names)
     * @return First publisher name, or null if none available
     */
    private String extractFirstPublisher(Object publisher) {
        if (publisher instanceof List && !((List<?>) publisher).isEmpty()) {
            return ((List<?>) publisher).get(0).toString();
        }
        return null;
    }

    /**
     * Extracts the cover image ID from the OpenLibrary document.
     * @param doc OpenLibrary document containing book metadata
     * @return Cover ID for building image URL, or null if no cover available
     */
    private String extractCoverKey(Map<String, Object> doc) {
        Object coverId = doc.get("cover_i");
        if (coverId instanceof Number) {
            return coverId.toString();
        }
        return null;
    }
}