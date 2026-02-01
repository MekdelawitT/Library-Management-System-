package library;

import java.time.LocalDate;

/**
 * Represents a borrowing transaction between a Member and a Book.
 * Domain entity that encapsulates borrowing state and behavior.
 */
public class BorrowedBook {

    private final Book book;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;

    public BorrowedBook(Book book, LocalDate dueDate) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null.");
        }
        if (dueDate == null || dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be null or in the past.");
        }
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = dueDate;
        this.returnDate = null;
    }

    /**
     * Constructor for loading from database.
     * Allows setting all fields including return date.
     */
    public BorrowedBook(Book book, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null.");
        }
        if (borrowDate == null || dueDate == null) {
            throw new IllegalArgumentException("Borrow date and due date cannot be null.");
        }
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue() {
        if (isReturned()) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }

    public long daysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return LocalDate.now().until(dueDate).getDays() * -1; // Convert negative days to positive
    }

    public void markReturned() {
        if (isReturned()) {
            throw new IllegalStateException("Book already returned.");
        }
        this.returnDate = LocalDate.now();
    }
}
