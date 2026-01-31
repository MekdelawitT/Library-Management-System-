package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-specific DataHandler implementation for Book entities.
 */
public class SQLiteBookHandler implements DataHandler<Book> {

    @Override
    public void saveData(List<Book> books) {
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            for (Book book : books) {
                if (bookExists(conn, book.getId())) {
                    updateBook(conn, book);
                } else {
                    insertBook(conn, book);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving books: " + e.getMessage());
        }
    }

    @Override
    public List<Book> readData() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, available, cover_path FROM books ORDER BY id";

        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    boolean available = rs.getInt("available") != 0;
                    String coverPath = rs.getString("cover_path");

                    Book book = new Book(id, title, author);
                    book.setAvailable(available);
                    if (coverPath != null && !coverPath.isBlank()) book.setCoverPath(coverPath);
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading books: " + e.getMessage());
        }

        return books;
    }

    @Override
    public void deleteData(int id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
        }
    }

    private boolean bookExists(Connection conn, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void insertBook(Connection conn, Book book) throws SQLException {
        String sql = "INSERT INTO books (id, title, author, available, cover_path) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setInt(4, book.isAvailable() ? 1 : 0);
            pstmt.setString(5, book.getCoverPath());
            pstmt.executeUpdate();
        }
    }

    private void updateBook(Connection conn, Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, available = ?, cover_path = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.isAvailable() ? 1 : 0);
            pstmt.setString(4, book.getCoverPath());
            pstmt.setInt(5, book.getId());
            pstmt.executeUpdate();
        }
    }
}
