package library;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteBookHandler extends SQLiteRepository<Book> {

    @Override
    protected String getTableName() {
        return "books";
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO books (id, title, author, available, cover_path) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE books SET title = ?, author = ?, available = ?, cover_path = ? WHERE id = ?";
    }

    @Override
    protected Book mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String author = rs.getString("author");
        boolean available = rs.getInt("available") != 0;
        String coverPath = rs.getString("cover_path");

        Book book = new Book(id, title, author);
        book.setAvailable(available);
        if (coverPath != null && !coverPath.isBlank()) {
            book.setCoverPath(coverPath);
        }
        return book;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Book book) throws SQLException {
        pstmt.setInt(1, book.getId());
        pstmt.setString(2, book.getTitle());
        pstmt.setString(3, book.getAuthor());
        pstmt.setInt(4, book.isAvailable() ? 1 : 0);
        pstmt.setString(5, book.getCoverPath());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Book book) throws SQLException {
        pstmt.setString(1, book.getTitle());
        pstmt.setString(2, book.getAuthor());
        pstmt.setInt(3, book.isAvailable() ? 1 : 0);
        pstmt.setString(4, book.getCoverPath());
        pstmt.setInt(5, book.getId());
    }
}
