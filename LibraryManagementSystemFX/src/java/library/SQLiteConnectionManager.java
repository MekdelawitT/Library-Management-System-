package library;

import java.io.File;
import java.sql.*;

public class SQLiteConnectionManager {
    private static final String DB_DIR = System.getProperty("user.home") + File.separator + "LibraryManagementSystem";
    private static final String DB_PATH = DB_DIR + File.separator + "library.db";
    private static final String DB_URL = "jdbc:sqlite:" + new File(DB_PATH).getAbsolutePath().replace("\\", "/");
    
    private static Connection connection;
    private static boolean initialized = false;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    private SQLiteConnectionManager() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            ensureDatabaseDirectoryExists();
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            if (!initialized) {
                initializeDatabase();
                initialized = true;
            }
        }
        return connection;
    }

    private static void ensureDatabaseDirectoryExists() {
        new File(DB_DIR).mkdirs();
    }

    public static String getDatabasePath() {
        return DB_PATH;
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
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT NOT NULL, available INTEGER NOT NULL DEFAULT 1, cover_path TEXT)",
            "CREATE TABLE IF NOT EXISTS members (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, password TEXT NOT NULL, balance REAL NOT NULL DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS borrowed_books (id INTEGER PRIMARY KEY AUTOINCREMENT, book_id INTEGER NOT NULL, member_id INTEGER NOT NULL, borrow_date TEXT NOT NULL, due_date TEXT NOT NULL, return_date TEXT, FOREIGN KEY (book_id) REFERENCES books(id), FOREIGN KEY (member_id) REFERENCES members(id))"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String table : tables) {
                stmt.execute(table);
            }
            
            try { stmt.execute("ALTER TABLE books ADD COLUMN cover_path TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE borrowed_books ADD COLUMN due_date TEXT"); } catch (SQLException ignored) {}
        }
    }
}
