package library;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for managing Book persistence.
 * SRP: Book data access only.
 * DIP: Delegates storage to DataHandler abstraction.
 */
public class BookRepository {

    private final DataHandler<Book> dataHandler;

    public BookRepository(DataHandler<Book> dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void save(Book book) {
        List<Book> all = new ArrayList<>(dataHandler.readData());

        // Remove existing instance if present
        all = all.stream()
                .filter(b -> b.getId() != book.getId())
                .collect(Collectors.toList());

        all.add(book);
        dataHandler.saveData(all);
    }

    public void deleteData(int id) {
        dataHandler.deleteData(id);
    }

    public List<Book> findAll() {
        return dataHandler.readData();
    }
}
