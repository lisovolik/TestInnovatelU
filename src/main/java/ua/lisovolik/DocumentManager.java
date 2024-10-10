package ua.lisovolik;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.id == null) {
            document.id = generateNewId();
        } else {
            Optional<Document> optional = findById(document.id);
            if (optional.isPresent()) {
                document.created = optional.get().created;
            }
        }
        storage.put(document.id, document);
        return document;
    }


    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> filterByTitlePrefixes(doc, request.titlePrefixes))
                .filter(doc -> filterByContent(doc, request.containsContents))
                .filter(doc -> filterByAuthorIds(doc, request.authorIds))
                .filter(doc -> filterByCreatedDate(doc, request.createdFrom, request.createdTo))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    private String generateNewId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private boolean filterByTitlePrefixes(Document document, List<String> titlePrefixes) {
        if (titlePrefixes == null || titlePrefixes.isEmpty()) {
            return true;
        }
        String title = document.title.toLowerCase();
        return titlePrefixes.stream()
                .map(String::toLowerCase)
                .anyMatch(title::startsWith);
    }

    private boolean filterByContent(Document document, List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return true;
        }
        String content = document.content.toLowerCase();
        return contents.stream()
                .map(String::toLowerCase)
                .anyMatch(content::contains);
    }

    private boolean filterByAuthorIds(Document document, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        }
        String authorId = document.author.id;
        return authorIds.stream()
                .map(String::toLowerCase)
                .anyMatch(id -> id.equals(authorId.toLowerCase()));
    }

    private boolean filterByCreatedDate(Document document, Instant createdFrom, Instant createdTo) {
        if (createdFrom != null && document.created.isBefore(createdFrom)) {
            return false;
        }
        if (createdTo != null && document.created.isAfter(createdTo)) {
            return false;
        }
        return true;
    }

    public int getDocumentCount() {
        return storage.size();
    }


    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}