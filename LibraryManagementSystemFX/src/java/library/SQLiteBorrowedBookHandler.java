package library;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SQLiteBorrowedBookHandler {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public void saveBorrowedBook(int memberId, BorrowedBook borrowedBook) {
        executeUpdate("INSERT INTO borrowed_books (member_id, book_id, borrow_date, due_date, return_date) VALUES (?, ?, ?, ?, ?)",
            memberId, borrowedBook.getBook().getId(),
            borrowedBook.getBorrowDate().format(DATE_FORMATTER),
            borrowedBook.getDueDate().format(DATE_FORMATTER),
            borrowedBook.getReturnDate() != null ? borrowedBook.getReturnDate().format(DATE_FORMATTER) : null);
    }
    
    public void markBookAsReturned(int bookId) {
        executeUpdate("UPDATE borrowed_books SET return_date = ? WHERE book_id = ? AND return_date IS NULL",
            LocalDate.now().format(DATE_FORMATTER), bookId);
    }
    
    public boolean isBookCurrentlyBorrowed(int bookId) {
        return executeQuery("SELECT COUNT(*) FROM borrowed_books WHERE book_id = ? AND return_date IS NULL", bookId)
            .map(rs -> rs.getInt(1) > 0).orElse(false);
    }
    
    public int getCurrentBorrowerId(int bookId) {
        return executeQuery("SELECT member_id FROM borrowed_books WHERE book_id = ? AND return_date IS NULL LIMIT 1", bookId)
            .map(rs -> rs.getInt("member_id")).orElse(-1);
    }
    
    public Integer findBorrowerIdForBook(int bookId) {
        return executeQuery("SELECT member_id FROM borrowed_books WHERE book_id = ? ORDER BY borrow_date DESC LIMIT 1", bookId)
            .map(rs -> rs.getInt("member_id")).orElse(null);
    }
    
    public LocalDate getDueDateForBook(int bookId) {
        return executeQuery("SELECT due_date FROM borrowed_books WHERE book_id = ? AND return_date IS NULL LIMIT 1", bookId)
            .map(rs -> parseDate(rs.getString("due_date"))).orElse(null);
    }
    
    public LocalDate getBorrowDateForBook(int bookId) {
        return executeQuery("SELECT borrow_date FROM borrowed_books WHERE book_id = ? AND return_date IS NULL LIMIT 1", bookId)
            .map(rs -> parseDate(rs.getString("borrow_date"))).orElse(null);
    }
    
    public List<BorrowedBook> loadBorrowedBooksForMember(int memberId, BookRepository bookRepository) {
        List<BorrowedBook> borrowedBooks = new ArrayList<>();
        
        executeQuery("SELECT book_id, borrow_date, due_date, return_date FROM borrowed_books WHERE member_id = ?", memberId)
            .ifPresent(rs -> {
                try {
                    while (rs.next()) {
                        int bookId = rs.getInt("book_id");
                        LocalDate borrowDate = LocalDate.parse(rs.getString("borrow_date"), DATE_FORMATTER);
                        LocalDate dueDate = LocalDate.parse(rs.getString("due_date"), DATE_FORMATTER);
                        String returnDateStr = rs.getString("return_date");
                        
                        Book book = bookRepository.findAll().stream()
                            .filter(b -> b.getId() == bookId)
                            .findFirst()
                            .orElse(null);
                        
                        if (book != null) {
                            LocalDate returnDate = returnDateStr != null ? 
                                LocalDate.parse(returnDateStr, DATE_FORMATTER) : null;
                            
                            BorrowedBook borrowedBook = returnDate != null ?
                                new BorrowedBook(book, borrowDate, dueDate, returnDate) :
                                new BorrowedBook(book, dueDate);
                            
                            borrowedBooks.add(borrowedBook);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error processing borrowed books: " + e.getMessage());
                }
            });
        
        return borrowedBooks;
    }
    
    private void executeUpdate(String sql, Object... params) {
        try (Connection conn = SQLiteConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
        }
    }
    
    private java.util.Optional<ResultSet> executeQuery(String sql, Object... params) {
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            return java.util.Optional.of(pstmt.executeQuery());
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return java.util.Optional.empty();
        }
    }
    
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }
}
