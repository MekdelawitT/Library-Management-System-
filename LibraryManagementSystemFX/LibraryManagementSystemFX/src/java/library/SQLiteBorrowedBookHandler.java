package library;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-specific handler for BorrowedBook operations.
 * Manages the borrowed_books table relationships between members and books.
 */
public class SQLiteBorrowedBookHandler {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Saves a borrowed book relationship to the database.
     * @param memberId The ID of the member borrowing the book
     * @param borrowedBook The BorrowedBook object containing book and borrowing details
     */
    public void saveBorrowedBook(int memberId, BorrowedBook borrowedBook) {
        String sql = "INSERT INTO borrowed_books (member_id, book_id, borrow_date, due_date, return_date) VALUES (?, ?, ?, ?, ?)";
        
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, memberId);
                pstmt.setInt(2, borrowedBook.getBook().getId());
                pstmt.setString(3, borrowedBook.getBorrowDate().format(DATE_FORMATTER));
                pstmt.setString(4, borrowedBook.getDueDate().format(DATE_FORMATTER));
                pstmt.setString(5, borrowedBook.getReturnDate() != null ? 
                    borrowedBook.getReturnDate().format(DATE_FORMATTER) : null);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error saving borrowed book: " + e.getMessage());
        }
    }
    
    /**
     * Marks a book as returned in the database by setting the return_date.
     * @param bookId The ID of the book being returned
     */
    public void markBookAsReturned(int bookId) {
        String sql = "UPDATE borrowed_books SET return_date = ? WHERE book_id = ? AND return_date IS NULL";
        
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, LocalDate.now().format(DATE_FORMATTER));
                pstmt.setInt(2, bookId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error marking book as returned: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a book is currently borrowed (has an active borrowing record).
     * @param bookId The ID of the book to check
     * @return true if the book is currently borrowed, false otherwise
     */
    public boolean isBookCurrentlyBorrowed(int bookId) {
        String sql = "SELECT COUNT(*) FROM borrowed_books WHERE book_id = ? AND return_date IS NULL";
        
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if book is borrowed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the member ID who currently has the book borrowed.
     * @param bookId The ID of the book
     * @return The member ID if the book is borrowed, -1 otherwise
     */
    public int getCurrentBorrowerId(int bookId) {
        String sql = "SELECT member_id FROM borrowed_books WHERE book_id = ? AND return_date IS NULL LIMIT 1";
        
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("member_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting current borrower: " + e.getMessage());
        }
        return -1; // Return -1 if no current borrower or error
    }
    
    /**
     * Finds the borrower ID for a specific book (including historical records).
     * @param bookId The ID of the book
     * @return The borrower ID if found, null otherwise
     */
    public Integer findBorrowerIdForBook(int bookId) {
        String sql = "SELECT member_id FROM borrowed_books WHERE book_id = ? ORDER BY borrow_date DESC LIMIT 1";
        
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("member_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding borrower ID for book: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Gets the due date for a currently borrowed book.
     * @param bookId The ID of the book
     * @return The due date if the book is borrowed, null otherwise
     */
    public LocalDate getDueDateForBook(int bookId) {
        String sql = "SELECT due_date FROM borrowed_books WHERE book_id = ? AND return_date IS NULL LIMIT 1";
        
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String dueDateStr = rs.getString("due_date");
                        if (dueDateStr != null) {
                            return LocalDate.parse(dueDateStr, DATE_FORMATTER);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting due date for book: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Loads all borrowed books for a member from the database.
     * @param memberId The ID of the member
     * @param bookRepository Repository to get Book objects
     * @return List of BorrowedBook objects
     */
    public List<BorrowedBook> loadBorrowedBooksForMember(int memberId, BookRepository bookRepository) {
        List<BorrowedBook> borrowedBooks = new ArrayList<>();
        String sql = "SELECT book_id, borrow_date, due_date, return_date FROM borrowed_books WHERE member_id = ?";
        
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, memberId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int bookId = rs.getInt("book_id");
                        LocalDate borrowDate = LocalDate.parse(rs.getString("borrow_date"), DATE_FORMATTER);
                        LocalDate dueDate = LocalDate.parse(rs.getString("due_date"), DATE_FORMATTER);
                        String returnDateStr = rs.getString("return_date");
                        
                        // Get the book from repository
                        Book book = bookRepository.findAll().stream()
                            .filter(b -> b.getId() == bookId)
                            .findFirst()
                            .orElse(null);
                        
                        if (book != null) {
                            BorrowedBook borrowedBook;
                            LocalDate returnDate = returnDateStr != null ? 
                                LocalDate.parse(returnDateStr, DATE_FORMATTER) : null;
                            
                            // Use appropriate constructor based on whether book is returned
                            if (returnDate != null) {
                                borrowedBook = new BorrowedBook(book, borrowDate, dueDate, returnDate);
                            } else {
                                borrowedBook = new BorrowedBook(book, dueDate);
                            }
                            
                            borrowedBooks.add(borrowedBook);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading borrowed books for member: " + e.getMessage());
        }
        
        return borrowedBooks;
    }
}
