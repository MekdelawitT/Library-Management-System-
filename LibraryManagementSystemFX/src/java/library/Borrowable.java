package library;

import java.time.LocalDate;

/**
 * Contract for borrowable items.
 * ISP: Only borrowing-related behavior is exposed.
 */
public interface Borrowable {

    /**
     * Marks the item as borrowed and initializes borrowing state.
     */
    void borrow();

    /**
     * Marks the item as returned and clears borrowing state.
     */
    void returnItem();

    /**
     * Generates a due date based on borrowing rules.
     */
    LocalDate generateDueDate();
}
