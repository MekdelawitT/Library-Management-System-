package library;

/**
 * Librarian user.
 * Delegates management and reporting actions to LibraryService.
 */
public class Librarian extends User {

    private final LibraryService libraryService;

    public Librarian(int id, String name, String password, LibraryService libraryService) {
        super(id, name, password);
        this.libraryService = libraryService;
    }

    /**
     * Adds a new book to the library.
     */
    public void addBook(Book book) {
        libraryService.addBook(book);
    }

    /**
     * Removes a book from the library.
     */
    public void removeBook(Book book) {
        libraryService.removeBook(book);
    }

    /**
     * Requests report data. Caller handles display.
     */
    public LibraryReport generateReport() {
        return libraryService.getReport();
    }
}
