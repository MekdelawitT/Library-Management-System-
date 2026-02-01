package library;

import java.time.LocalDate;

/**
 * Domain entity representing a library book.
 * Implements Borrowable: books are the items borrowed and returned.
 * coverPath: file path to cover image (stored as TEXT in SQLite, not BLOB).
 */
public class Book implements Borrowable, Identifiable {

    private final int id;
    private final String title;
    private final String author;
    private boolean available = true;
    /** File path to cover image (TEXT in DB). Null or empty = no cover. */
    private String coverPath;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverPath = null;
    }

    @Override
    public void borrow() {
        this.available = false;
    }

    @Override
    public void returnItem() {
        this.available = true;
    }

    @Override
    public LocalDate generateDueDate() {
        return LocalDate.now().plusWeeks(2);
    }

    @Override
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /** Path to cover image file (TEXT in DB). */
    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
}
