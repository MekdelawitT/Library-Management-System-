package library;

import java.util.List;

/**
 * Contract for data persistence operations.
 * Dependency Inversion: High-level modules depend on this abstraction.
 */
public interface DataHandler<T> {

    void saveData(List<T> data);

    List<T> readData();

    void deleteData(int id);
}
