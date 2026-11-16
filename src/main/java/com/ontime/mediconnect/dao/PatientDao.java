package com.ontime.mediconnect.dao;

import com.ontime.mediconnect.model.Patient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database access for the Patient table. */
public class PatientDao {

    public List<Patient> findAll() throws SQLException {
        return search("");
    }

    /** Case-insensitive search on ID, name or address. */
    public List<Patient> search(String text) throws SQLException {
        String sql = "SELECT PatientID, Name, Address, Gender, Age, BloodGroup, Weight, Height FROM Patient "
                + "WHERE PatientID LIKE ? OR Name LIKE ? OR Address LIKE ? ORDER BY PatientID";
        String like = "%" + text.trim() + "%";
        List<Patient> result = new ArrayList<>();
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Patient(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getInt(5), rs.getString(6), rs.getDouble(7), rs.getDouble(8)));
                }
            }
        }
        return result;
    }

    public Patient insert(Patient p) throws SQLException {
        String sql = "INSERT INTO Patient (PatientID, Name, Address, Gender, Age, BloodGroup, Weight, Height) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = Database.connect()) {
            String id = IdGenerator.next(con, "Patient", "PatientID", "P");
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, id, p);
                ps.executeUpdate();
            }
            return new Patient(id, p.name(), p.address(), p.gender(), p.age(), p.bloodGroup(),
                    p.weight(), p.height());
        }
    }

    public boolean update(Patient p) throws SQLException {
        String sql = "UPDATE Patient SET Name=?, Address=?, Gender=?, Age=?, BloodGroup=?, Weight=?, Height=? "
                + "WHERE PatientID=?";
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.name().trim());
            ps.setString(2, p.address().trim());
            ps.setString(3, p.gender());
            ps.setInt(4, p.age());
            ps.setString(5, p.bloodGroup());
            ps.setDouble(6, p.weight());
            ps.setDouble(7, p.height());
            ps.setString(8, p.id());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection con = Database.connect();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Patient WHERE PatientID=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static void bind(PreparedStatement ps, String id, Patient p) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, p.name().trim());
        ps.setString(3, p.address().trim());
        ps.setString(4, p.gender());
        ps.setInt(5, p.age());
        ps.setString(6, p.bloodGroup());
        ps.setDouble(7, p.weight());
        ps.setDouble(8, p.height());
    }
}
