package library;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataHandler implementation for database persistence.
 * Holds Connection per diagram; stub uses in-memory store when conn is null.
 * DIP: Callers depend on DataHandler, not this concrete class.
 */
public class DatabaseHandler<T> implements DataHandler<T> {

    @SuppressWarnings("unused")
    private final Connection conn;
    private final ConcurrentHashMap<Integer, T> store = new ConcurrentHashMap<>();
    private final IdAccessor<T> idAccessor;

    public interface IdAccessor<T> {
        int getId(T entity);
    }

    public DatabaseHandler(Connection conn, IdAccessor<T> idAccessor) {
        this.conn = conn;
        this.idAccessor = idAccessor;
    }

    /** In-memory stub when no DB connection available. */
    public DatabaseHandler(IdAccessor<T> idAccessor) {
        this(null, idAccessor);
    }

    @Override
    public void saveData(List<T> data) {
        for (T entity : data) {
            store.put(idAccessor.getId(entity), entity);
        }
    }

    @Override
    public List<T> readData() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteData(int id) {
        store.remove(id);
    }
}
