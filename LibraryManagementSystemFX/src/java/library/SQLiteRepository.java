package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class SQLiteRepository<T extends Identifiable> implements DataHandler<T> {
    
    protected abstract String getTableName();
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    protected abstract void setInsertParameters(PreparedStatement pstmt, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement pstmt, T entity) throws SQLException;
    
    @Override
    public void saveData(List<T> entities) {
        try (Connection conn = SQLiteConnectionManager.getConnection()) {
            for (T entity : entities) {
                if (exists(conn, entity.getId())) {
                    update(conn, entity);
                } else {
                    insert(conn, entity);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving " + getTableName() + ": " + e.getMessage());
        }
    }
    
    @Override
    public List<T> readData() {
        List<T> entities = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY id";
        
        try (Connection conn = SQLiteConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entities.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading " + getTableName() + ": " + e.getMessage());
        }
        
        return entities;
    }
    
    @Override
    public void deleteData(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        
        try (Connection conn = SQLiteConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting " + getTableName() + ": " + e.getMessage());
        }
    }
    
    private boolean exists(Connection conn, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private void insert(Connection conn, T entity) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(getInsertSQL())) {
            setInsertParameters(pstmt, entity);
            pstmt.executeUpdate();
        }
    }
    
    private void update(Connection conn, T entity) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(getUpdateSQL())) {
            setUpdateParameters(pstmt, entity);
            pstmt.executeUpdate();
        }
    }
}
