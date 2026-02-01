package library;

import java.sql.*;

public class SQLiteConnectionManager {
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection connection;

    private SQLiteConnectionManager() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
        }
        return connection;
    }

    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    private static void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT NOT NULL, available INTEGER NOT NULL DEFAULT 1, cover_path TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS members (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, password TEXT NOT NULL, balance REAL NOT NULL DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS borrowed_books (id INTEGER PRIMARY KEY AUTOINCREMENT, book_id INTEGER NOT NULL, member_id INTEGER NOT NULL, borrow_date TEXT NOT NULL, due_date TEXT NOT NULL, return_date TEXT, FOREIGN KEY (book_id) REFERENCES books(id), FOREIGN KEY (member_id) REFERENCES members(id))");
        }
    }
}
