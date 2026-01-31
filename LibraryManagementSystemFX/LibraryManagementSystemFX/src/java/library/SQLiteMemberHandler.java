package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-specific DataHandler implementation for Member entities.
 */
public class SQLiteMemberHandler implements DataHandler<Member> {

    @Override
    public void saveData(List<Member> members) {
        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            for (Member member : members) {
                if (memberExists(conn, member.getId())) {
                    updateMember(conn, member);
                } else {
                    insertMember(conn, member);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving members: " + e.getMessage());
        }
    }

    @Override
    public List<Member> readData() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT id, name, password, balance FROM members ORDER BY id";

        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String password = rs.getString("password");
                    double balance = rs.getDouble("balance");

                    Member member = new Member(id, name, password);
                    member.setBalance(balance);
                    members.add(member);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading members: " + e.getMessage());
        }

        return members;
    }

    @Override
    public void deleteData(int id) {
        String sql = "DELETE FROM members WHERE id = ?";

        try {
            Connection conn = SQLiteConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error deleting member: " + e.getMessage());
        }
    }

    private boolean memberExists(Connection conn, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM members WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void insertMember(Connection conn, Member member) throws SQLException {
        String sql = "INSERT INTO members (id, name, password, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, member.getId());
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getPassword());
            pstmt.setDouble(4, member.getBalance());
            pstmt.executeUpdate();
        }
    }

    private void updateMember(Connection conn, Member member) throws SQLException {
        String sql = "UPDATE members SET name = ?, password = ?, balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPassword());
            pstmt.setDouble(3, member.getBalance());
            pstmt.setInt(4, member.getId());
            pstmt.executeUpdate();
        }
    }
}
