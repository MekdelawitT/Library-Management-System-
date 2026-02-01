package library;

import java.util.List;

/**
 * Simple test class to verify SQLite integration works correctly.
 */
public class SQLiteTest {
    public static void main(String[] args) {
        try {
            // Initialize handlers
            SQLiteBookHandler bookHandler = new SQLiteBookHandler();
            SQLiteMemberHandler memberHandler = new SQLiteMemberHandler();

            // Test creating and saving a book
            Book testBook = new Book(1, "Test Book", "Test Author");
            testBook.setAvailable(true);
            
            // Test creating and saving a member
            Member testMember = new Member(1, "Test Member", "password123");

            // Save data
            bookHandler.saveData(List.of(testBook));
            memberHandler.saveData(List.of(testMember));

            // Read data back
            List<Book> books = bookHandler.readData();
            List<Member> members = memberHandler.readData();

            // Verify data
            System.out.println("=== SQLite Integration Test Results ===");
            System.out.println("Books found: " + books.size());
            for (Book book : books) {
                System.out.println("Book: " + book.getTitle() + " by " + book.getAuthor() + 
                                 " (Available: " + book.isAvailable() + ")");
            }

            System.out.println("Members found: " + members.size());
            for (Member member : members) {
                System.out.println("Member: " + member.getName() + " (ID: " + member.getId() + ")");
            }

            // Test update
            testBook.setAvailable(false);
            bookHandler.saveData(List.of(testBook));
            
            // Test delete
            bookHandler.deleteData(1);
            memberHandler.deleteData(1);

            System.out.println("\n=== After Update/Delete Test ===");
            books = bookHandler.readData();
            members = memberHandler.readData();
            System.out.println("Books remaining: " + books.size());
            System.out.println("Members remaining: " + members.size());

            // Close connection
            SQLiteConnectionManager.closeConnection();
            System.out.println("\nSQLite integration test completed successfully!");

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
