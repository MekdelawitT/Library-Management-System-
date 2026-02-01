package library;

/**
 * DTO holding report data. No UI logic—presentation is the caller's responsibility.
 */
public final class LibraryReport {

    private final int totalBooks;
    private final int totalMembers;

    public LibraryReport(int totalBooks, int totalMembers) {
        this.totalBooks = totalBooks;
        this.totalMembers = totalMembers;
    }

    public int getTotalBooks() {
        return totalBooks;
    }

    public int getTotalMembers() {
        return totalMembers;
    }
}
