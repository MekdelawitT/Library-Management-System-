package library;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteMemberHandler extends SQLiteRepository<Member> {

    @Override
    protected String getTableName() {
        return "members";
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO members (id, name, password, balance) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE members SET name = ?, password = ?, balance = ? WHERE id = ?";
    }

    @Override
    protected Member mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String password = rs.getString("password");
        double balance = rs.getDouble("balance");

        Member member = new Member(id, name, password);
        member.setBalance(balance);
        return member;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Member member) throws SQLException {
        pstmt.setInt(1, member.getId());
        pstmt.setString(2, member.getName());
        pstmt.setString(3, member.getPassword());
        pstmt.setDouble(4, member.getBalance());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Member member) throws SQLException {
        pstmt.setString(1, member.getName());
        pstmt.setString(2, member.getPassword());
        pstmt.setDouble(3, member.getBalance());
        pstmt.setInt(4, member.getId());
    }
}
