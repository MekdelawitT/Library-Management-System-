package library;

/**
 * Marks entities that have a unique integer ID.
 * Used by DataHandler implementations for type-safe deleteData(id).
 */
public interface Identifiable {
    int getId();
}
