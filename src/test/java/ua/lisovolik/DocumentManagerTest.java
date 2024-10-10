package ua.lisovolik;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Alexandr Lisovolik on  10.10.2024
 */

class DocumentManagerTest {

    @Test
    void save() {
        DocumentManager manager = new DocumentManager();

        DocumentManager.Author author = new DocumentManager.Author("1", "Author Name");
        DocumentManager.Document doc = new DocumentManager.Document("1", "Test Title", "Test Content", author, Instant.now());

        DocumentManager.Document savedDoc = manager.save(doc);

        assertEquals("1", savedDoc.getId());
        assertEquals("Test Title", savedDoc.getTitle());
        assertEquals("Test Content", savedDoc.getContent());
        assertEquals("1", savedDoc.getAuthor().getId());

        //replace doc with same id
        DocumentManager.Document docReplace = new DocumentManager.Document("1", "Test Title Replaced", "Test Content", author, Instant.now());
        manager.save(docReplace);
        assertEquals(1, manager.getDocumentCount(), "Incorrect documents count; Count should be 1.");

    }

    @Test
    void findById() {
        DocumentManager manager = new DocumentManager();

        DocumentManager.Author author = new DocumentManager.Author("1", "Author Name");
        DocumentManager.Document doc = new DocumentManager.Document("1", "Test Title", "Test Content", author, Instant.now());
        manager.save(doc);

        Optional<DocumentManager.Document> foundDoc = manager.findById("1");
        assertTrue(foundDoc.isPresent());
        assertEquals("Test Title", foundDoc.get().getTitle());

        Optional<DocumentManager.Document> notFoundDoc = manager.findById("2");
        assertFalse(notFoundDoc.isPresent());
    }

    @Test
    void search() {
        DocumentManager manager = new DocumentManager();

        DocumentManager.Author author1 = new DocumentManager.Author("1", "John Doe");
        DocumentManager.Author author2 = new DocumentManager.Author("2", "Jane Doe");

        DocumentManager.Document doc1 = new DocumentManager.Document("1", "One", "This is one", author1, Instant.now());
        DocumentManager.Document doc2 = new DocumentManager.Document("2", "Two", "This is two", author2, Instant.now());
        DocumentManager.Document doc3 = new DocumentManager.Document("3", "One and Three", "This is one and three", author1, Instant.now());

        manager.save(doc1);
        manager.save(doc2);
        manager.save(doc3);

        //search by title
        DocumentManager.SearchRequest titleRequest = new DocumentManager.SearchRequest(
                List.of("One"),
                null,
                null,
                null,
                null);
        List<DocumentManager.Document> titleResult = manager.search(titleRequest);

        assertEquals(2, titleResult.size());
        assertTrue(titleResult.stream().anyMatch(doc -> doc.getTitle().equals("One")));
        assertTrue(titleResult.stream().anyMatch(doc -> doc.getTitle().equals("One and Three")));

        //search by content
        DocumentManager.SearchRequest contentRequest = new DocumentManager.SearchRequest(
                null,
                List.of("one"),
                null,
                null,
                null);
        List<DocumentManager.Document> contentResults = manager.search(contentRequest);

        assertEquals(2, contentResults.size());
        assertTrue(contentResults.stream().anyMatch(doc -> doc.getTitle().equals("One")));
        assertTrue(contentResults.stream().anyMatch(doc -> doc.getTitle().equals("One and Three")));

        //search by author id
        DocumentManager.SearchRequest authorRequest = new DocumentManager.SearchRequest(
                null,
                null,
                List.of("1"),
                null,
                null);
        List<DocumentManager.Document> authorResults = manager.search(authorRequest);

        assertEquals(2, authorResults.size());
        assertTrue(authorResults.stream().anyMatch(doc -> doc.getTitle().equals("One")));
        assertTrue(authorResults.stream().anyMatch(doc -> doc.getTitle().equals("One and Three")));

        //search by date
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now().plusSeconds(3600);
        DocumentManager.SearchRequest dateRequest = new DocumentManager.SearchRequest(
                null,
                null,
                null,
                from,
                to
        );

        List<DocumentManager.Document> dateResults = manager.search(dateRequest);
        assertEquals(3, dateResults.size());
    }
}