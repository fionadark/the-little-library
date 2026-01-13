package thelittlelibrary.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a book in a user's personal library.
 * Stored in Firestore under the "books" collection.
 */
public class Book {
    
    private String id;              // Firestore document ID
    private String userId;          // Owner of this book
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private String publisher;
    private String coverUrl;
    
    // Library-specific fields
    private String readingStatus;   // "to-read", "reading", "completed"
    private String location;        // Physical location (e.g., "Living room shelf")
    private String personalNotes;   // User's notes about the book
    private Instant dateAdded;      // When added to library
    
    // Constructors
    public Book() {
        this.dateAdded = Instant.now();
        this.readingStatus = "to-read"; // Default status
    }

    /**
     * Converts Book object to a Map for Firestore storage.
     */
    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> map = new HashMap<>();
        if (userId != null) map.put("userId", userId);
        if (title != null) map.put("title", title);
        if (author != null) map.put("author", author);
        if (isbn != null) map.put("isbn", isbn);
        if (publicationYear != null) map.put("publicationYear", publicationYear);
        if (publisher != null) map.put("publisher", publisher);
        if (coverUrl != null) map.put("coverUrl", coverUrl);
        if (readingStatus != null) map.put("readingStatus", readingStatus);
        if (location != null) map.put("location", location);
        if (personalNotes != null) map.put("personalNotes", personalNotes);
        if (dateAdded != null) map.put("dateAdded", dateAdded.toString());
        return map;
    }

    /**
     * Creates Book object from Firestore document data.
     */
    public static Book fromFirestoreMap(String documentId, Map<String, Object> data) {
        Book book = new Book();
        book.setId(documentId);
        book.setUserId((String) data.get("userId"));
        book.setTitle((String) data.get("title"));
        book.setAuthor((String) data.get("author"));
        book.setIsbn((String) data.get("isbn"));
        
        Object pubYear = data.get("publicationYear");
        if (pubYear instanceof Number) {
            book.setPublicationYear(((Number) pubYear).intValue());
        }
        
        book.setPublisher((String) data.get("publisher"));
        book.setCoverUrl((String) data.get("coverUrl"));
        book.setReadingStatus((String) data.get("readingStatus"));
        book.setLocation((String) data.get("location"));
        book.setPersonalNotes((String) data.get("personalNotes"));
        
        String dateAddedStr = (String) data.get("dateAdded");
        if (dateAddedStr != null) {
            book.setDateAdded(Instant.parse(dateAddedStr));
        }
        
        return book;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getReadingStatus() { return readingStatus; }
    public void setReadingStatus(String readingStatus) { this.readingStatus = readingStatus; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPersonalNotes() { return personalNotes; }
    public void setPersonalNotes(String personalNotes) { this.personalNotes = personalNotes; }

    public Instant getDateAdded() { return dateAdded; }
    public void setDateAdded(Instant dateAdded) { this.dateAdded = dateAdded; }
}
