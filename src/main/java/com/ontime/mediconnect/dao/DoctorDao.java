package com.ontime.mediconnect.dao;

import com.ontime.mediconnect.model.Doctor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database access for the Doctor table. */
public class DoctorDao {

    public List<Doctor> findAll() throws SQLException {
        return search("");
    }

    /** Case-insensitive search on ID, name or specialization. */
    public List<Doctor> search(String text) throws SQLException {
        String sql = "SELECT DoctorID, Name, Specialization, PhoneNo, EmailID FROM Doctor "
                + "WHERE DoctorID LIKE ? OR Name LIKE ? OR Specialization LIKE ? ORDER BY DoctorID";
        String like = "%" + text.trim() + "%";
        List<Doctor> result = new ArrayList<>();
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Doctor(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
                }
            }
        }
        return result;
    }

    /** Inserts the doctor with a newly generated ID and returns the saved record. */
    public Doctor insert(Doctor d) throws SQLException {
        String sql = "INSERT INTO Doctor (DoctorID, Name, Specialization, PhoneNo, EmailID) VALUES (?,?,?,?,?)";
        try (Connection con = Database.connect()) {
            String id = IdGenerator.next(con, "Doctor", "DoctorID", "D");
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, d.name().trim());
                ps.setString(3, d.specialization().trim());
                ps.setString(4, d.phone().trim());
                ps.setString(5, d.email().trim());
                ps.executeUpdate();
            }
            return new Doctor(id, d.name(), d.specialization(), d.phone(), d.email());
        }
    }

    /** @return true when a row was updated */
    public boolean update(Doctor d) throws SQLException {
        String sql = "UPDATE Doctor SET Name=?, Specialization=?, PhoneNo=?, EmailID=? WHERE DoctorID=?";
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.name().trim());
            ps.setString(2, d.specialization().trim());
            ps.setString(3, d.phone().trim());
            ps.setString(4, d.email().trim());
            ps.setString(5, d.id());
            return ps.executeUpdate() == 1;
        }
    }

    /** @return true when a row was deleted */
    public boolean delete(String id) throws SQLException {
        try (Connection con = Database.connect();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Doctor WHERE DoctorID=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
