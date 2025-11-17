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
 * Simplified service to search books using OpenLibrary API.
 */
@Service
public class ExternalBookSearchService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalBookSearchService.class);

    @Value("${external.api.open-library.base-url:https://openlibrary.org}")
    private String openLibraryBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Search for books by query.
     * 
     * @param query Search query (title, author, or general search)
     * @param limit Maximum number of results
     * @return List of books from OpenLibrary
     */
    public List<BookSearchResponse> searchBooks(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String url = String.format("%s/search.json?q=%s&limit=%d", 
                openLibraryBaseUrl, 
                query.replace(" ", "+"), 
                limit);

            logger.info("Searching OpenLibrary: {}", query);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || !response.containsKey("docs")) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");
            List<BookSearchResponse> books = new ArrayList<>();

            for (Map<String, Object> doc : docs) {
                BookSearchResponse book = new BookSearchResponse();
                
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

                books.add(book);
            }

            logger.info("Found {} books for query: {}", books.size(), query);
            return books;

        } catch (Exception e) {
            logger.error("OpenLibrary search failed for '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String extractFirstAuthor(Object authorName) {
        if (authorName instanceof List && !((List<?>) authorName).isEmpty()) {
            return ((List<?>) authorName).get(0).toString();
        }
        return null;
    }

    private String extractFirstIsbn(Object isbn) {
        if (isbn instanceof List && !((List<?>) isbn).isEmpty()) {
            return ((List<?>) isbn).get(0).toString();
        }
        return null;
    }

    private Integer extractYear(Object year) {
        if (year instanceof Number) {
            return ((Number) year).intValue();
        }
        return null;
    }

    private String extractFirstPublisher(Object publisher) {
        if (publisher instanceof List && !((List<?>) publisher).isEmpty()) {
            return ((List<?>) publisher).get(0).toString();
        }
        return null;
    }

    private String extractCoverKey(Map<String, Object> doc) {
        Object coverId = doc.get("cover_i");
        if (coverId instanceof Number) {
            return coverId.toString();
        }
        return null;
    }
}