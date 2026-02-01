package library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Library member.
 * Inherits from User and maintains a list of borrowed books.
 * Business rules are delegated to LibraryService.
 */
public class Member extends User {

    private double balance;
    private final List<BorrowedBook> borrowedBooks = new ArrayList<>();

    public Member(int id, String name, String password) {
        super(id, name, password);
        this.balance = 0.0;
    }

    /**
     * Registers a borrowed book with this member.
     * Actual borrowing rules are handled by LibraryService.
     */
    public void borrowBook(BorrowedBook borrowedBook) {
        if (borrowedBook == null) {
            throw new IllegalArgumentException("Borrowed book cannot be null.");
        }
        borrowedBooks.add(borrowedBook);
    }

    /**
     * Removes a borrowed book from this member.
     */
    public void returnBook(BorrowedBook borrowedBook) {
        if (!borrowedBooks.remove(borrowedBook)) {
            throw new IllegalStateException("This book was not borrowed by the member.");
        }
    }

    public void payFine(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        balance = Math.max(0, balance - amount);
    }

    public void addFine(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Fine amount cannot be negative.");
        }
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }

    /** Used when loading member from database. */
    public void setBalance(double balance) {
        this.balance = balance >= 0 ? balance : 0;
    }

    public List<BorrowedBook> getBorrowedBooks() {
        return Collections.unmodifiableList(borrowedBooks);
    }
}
