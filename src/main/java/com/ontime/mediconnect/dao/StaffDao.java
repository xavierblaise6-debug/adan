package com.ontime.mediconnect.dao;

import com.ontime.mediconnect.model.Staff;
import com.ontime.mediconnect.util.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Staff authentication. */
public class StaffDao {

    /** Returns the staff member when the ID and password match, otherwise empty. */
    public Optional<Staff> authenticate(String staffId, String password) throws SQLException {
        String sql = "SELECT StaffName, PasswordHash FROM Staff WHERE StaffID = ?";
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordHasher.verify(password, rs.getString("PasswordHash"))) {
                    return Optional.of(new Staff(staffId, rs.getString("StaffName")));
                }
                return Optional.empty();
            }
        }
    }

    /** Creates the default administrator (S001) when the Staff table is empty. */
    public void ensureDefaultAdmin() throws SQLException {
        try (Connection con = Database.connect()) {
            try (PreparedStatement count = con.prepareStatement("SELECT COUNT(*) FROM Staff");
                 ResultSet rs = count.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return;
            }
            String sql = "INSERT INTO Staff (StaffID, StaffName, PasswordHash) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "S001");
                ps.setString(2, "Administrator");
                ps.setString(3, PasswordHasher.hash("Admin@123"));
                ps.executeUpdate();
            }
        }
    }
}
