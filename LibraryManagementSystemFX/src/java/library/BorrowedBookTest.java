package library;

import java.util.List;

/**
 * Test class to verify borrowed book persistence works correctly.
 */
public class BorrowedBookTest {
    public static void main(String[] args) {
        try {
            // Initialize handlers
            SQLiteBookHandler bookHandler = new SQLiteBookHandler();
            SQLiteMemberHandler memberHandler = new SQLiteMemberHandler();
            SQLiteBorrowedBookHandler borrowedBookHandler = new SQLiteBorrowedBookHandler();
            
            // Create repositories
            BookRepository bookRepository = new BookRepository(bookHandler);
            MemberRepository memberRepository = new MemberRepository(memberHandler);
            LibraryService libraryService = new LibraryService(memberRepository, bookRepository);
            
            // Clean up any existing test data
            bookHandler.deleteData(999);
            bookHandler.deleteData(998);
            memberHandler.deleteData(999);
            
            // Create test book and member
            Book testBook = new Book(999, "Test Borrow Book", "Test Author");
            Member testMember = new Member(999, "Test Borrower", "password123");
            
            // Save test data
            libraryService.addBook(testBook);
            libraryService.registerMember(testMember);
            
            System.out.println("=== Before Borrowing ===");
            System.out.println("Book available: " + testBook.isAvailable());
            System.out.println("Member borrowed books: " + testMember.getBorrowedBooks().size());
            
            // Test borrowing
            java.time.LocalDate dueDate = java.time.LocalDate.now().plusWeeks(2);
            libraryService.borrowBook(testMember, testBook, dueDate);
            
            System.out.println("\n=== After Borrowing ===");
            System.out.println("Book available: " + testBook.isAvailable());
            System.out.println("Member borrowed books: " + testMember.getBorrowedBooks().size());
            
            // Test database persistence by reloading data
            System.out.println("\n=== Testing Database Persistence ===");
            
            // Create new handlers to simulate app restart
            SQLiteMemberHandler newMemberHandler = new SQLiteMemberHandler();
            SQLiteBookHandler newBookHandler = new SQLiteBookHandler();
            BookRepository newBookRepository = new BookRepository(newBookHandler);
            MemberRepository newMemberRepository = new MemberRepository(newMemberHandler);
            
            List<Member> reloadedMembers = newMemberRepository.findAll();
            List<Book> reloadedBooks = newBookRepository.findAll();
            
            Member reloadedMember = reloadedMembers.stream()
                .filter(m -> m.getId() == 999)
                .findFirst()
                .orElse(null);
                
            Book reloadedBook = reloadedBooks.stream()
                .filter(b -> b.getId() == 999)
                .findFirst()
                .orElse(null);
            
            if (reloadedMember != null && reloadedBook != null) {
                System.out.println("Reloaded book available: " + reloadedBook.isAvailable());
                System.out.println("Reloaded member borrowed books: " + reloadedMember.getBorrowedBooks().size());
                
                if (reloadedMember.getBorrowedBooks().size() > 0) {
                    BorrowedBook borrowedBook = reloadedMember.getBorrowedBooks().get(0);
                    System.out.println("Borrowed book title: " + borrowedBook.getBook().getTitle());
                    System.out.println("SUCCESS: Borrower information persisted correctly!");
                } else {
                    System.out.println("FAILURE: Borrowed books not loaded from database!");
                }
                
                // Test finding borrower by book ID
                Integer borrowerId = borrowedBookHandler.findBorrowerIdForBook(999);
                if (borrowerId != null) {
                    System.out.println("Borrower ID found for book: " + borrowerId);
                } else {
                    System.out.println("FAILURE: Could not find borrower ID for book!");
                }
                
                // Test returning
                libraryService.returnBook(reloadedMember, reloadedBook);
                System.out.println("\n=== After Return ===");
                System.out.println("Book available: " + reloadedBook.isAvailable());
                System.out.println("Member borrowed books: " + reloadedMember.getBorrowedBooks().size());
                
            } else {
                System.out.println("FAILURE: Could not reload test data!");
            }
            
            // Clean up test data
            bookHandler.deleteData(999);
            memberHandler.deleteData(999);
            
            // Close connection
            SQLiteConnectionManager.closeConnection();
            System.out.println("\nBorrowed book persistence test completed!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
