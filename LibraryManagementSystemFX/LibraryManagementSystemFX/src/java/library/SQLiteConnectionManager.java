package library;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite database connection manager.
 * Uses a fixed absolute path so data persists after closing the IDE or rebuilding.
 * Database file is stored in user home (e.g. C:\Users\YourName\LibraryManagementSystem\library.db).
 */
public class SQLiteConnectionManager {
    /** Fixed absolute path: avoids in-memory DB and build/output folders. Data persists across restarts. */
    private static final String DB_DIR = System.getProperty("user.home") + File.separator + "LibraryManagementSystem";
    private static final String DB_PATH = DB_DIR + File.separator + "library.db";
    /** JDBC URL with forward slashes so SQLite finds the same file on Windows after IDE restart. */
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

    /**
     * Gets the database connection, creating it if necessary.
     * @return Connection to SQLite database
     * @throws SQLException if connection fails
     */
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

    /** Creates the database directory if it does not exist (required for absolute path). */
    private static void ensureDatabaseDirectoryExists() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /** Returns the absolute path where the database file is stored (for debugging / user info). */
    public static String getDatabasePath() {
        return DB_PATH;
    }

    /**
     * Closes the database connection.
     */
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

    /**
     * Initializes database tables if they don't exist.
     * Schema matches Book, Member and borrowed_books usage.
     */
    private static void initializeDatabase() throws SQLException {
        String createBooksTable = """
            CREATE TABLE IF NOT EXISTS books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                available INTEGER NOT NULL DEFAULT 1,
                cover_path TEXT
            )
        """;

        String createMembersTable = """
            CREATE TABLE IF NOT EXISTS members (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                password TEXT NOT NULL,
                balance REAL NOT NULL DEFAULT 0
            )
        """;

        String createBorrowedBooksTable = """
            CREATE TABLE IF NOT EXISTS borrowed_books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                member_id INTEGER NOT NULL,
                borrow_date TEXT NOT NULL,
                due_date TEXT NOT NULL,
                return_date TEXT,
                FOREIGN KEY (book_id) REFERENCES books(id),
                FOREIGN KEY (member_id) REFERENCES members(id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createBooksTable);
            stmt.execute(createMembersTable);
            stmt.execute(createBorrowedBooksTable);
            // Add cover_path to existing books table if missing (TEXT = file path, not BLOB)
            try {
                stmt.execute("ALTER TABLE books ADD COLUMN cover_path TEXT");
            } catch (SQLException e) {
                // Column already exists on existing DB
            }
            // Add due_date to existing borrowed_books table if missing
            try {
                stmt.execute("ALTER TABLE borrowed_books ADD COLUMN due_date TEXT");
            } catch (SQLException e) {
                // Column already exists on existing DB
            }
        }
    }
}
