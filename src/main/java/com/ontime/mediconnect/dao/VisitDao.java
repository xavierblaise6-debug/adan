package com.ontime.mediconnect.dao;

import com.ontime.mediconnect.model.Visit;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Database access for the PatientDetails table (visits, diagnosis and treatment). */
public class VisitDao {
    private static final String SELECT =
            "SELECT PatientDetailsID, DoctorID, PatientID, Ailment, Description, CheckupVisitNumber, "
            + "CheckupDate, ConsFees, OtherExpenses FROM PatientDetails ";

    public List<Visit> findAll() throws SQLException {
        return query(SELECT + "ORDER BY PatientDetailsID", null);
    }

    public List<Visit> findByDate(LocalDate date) throws SQLException {
        return query(SELECT + "WHERE CheckupDate = ? ORDER BY PatientDetailsID", Date.valueOf(date));
    }

    /** Complete treatment history of one patient, oldest first. */
    public List<Visit> findByPatient(String patientId) throws SQLException {
        return query(SELECT + "WHERE PatientID = ? ORDER BY CheckupDate, CheckupVisitNumber", patientId);
    }

    /** Visits that have not been billed yet. */
    public List<Visit> findUnbilled() throws SQLException {
        return query(SELECT + "WHERE PatientDetailsID NOT IN (SELECT PatientDetailsID FROM Bill) "
                + "ORDER BY PatientDetailsID", null);
    }

    /** The visit number the patient's next visit will have (previous visits + 1). */
    public int nextVisitNumber(String patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PatientDetails WHERE PatientID = ?";
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) + 1;
            }
        }
    }

    public Visit insert(Visit v) throws SQLException {
        String sql = "INSERT INTO PatientDetails (PatientDetailsID, DoctorID, PatientID, Ailment, Description, "
                + "CheckupVisitNumber, CheckupDate, ConsFees, OtherExpenses) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = Database.connect()) {
            String id = IdGenerator.next(con, "PatientDetails", "PatientDetailsID", "V");
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, v.doctorId());
                ps.setString(3, v.patientId());
                ps.setString(4, v.ailment().trim());
                ps.setString(5, v.description() == null ? null : v.description().trim());
                ps.setInt(6, v.visitNumber());
                ps.setDate(7, Date.valueOf(v.checkupDate()));
                ps.setBigDecimal(8, v.consFees());
                ps.setBigDecimal(9, v.otherExpenses());
                ps.executeUpdate();
            }
            return new Visit(id, v.doctorId(), v.patientId(), v.ailment(), v.description(),
                    v.visitNumber(), v.checkupDate(), v.consFees(), v.otherExpenses());
        }
    }

    public boolean update(Visit v) throws SQLException {
        String sql = "UPDATE PatientDetails SET DoctorID=?, Ailment=?, Description=?, CheckupDate=?, "
                + "ConsFees=?, OtherExpenses=? WHERE PatientDetailsID=?";
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.doctorId());
            ps.setString(2, v.ailment().trim());
            ps.setString(3, v.description() == null ? null : v.description().trim());
            ps.setDate(4, Date.valueOf(v.checkupDate()));
            ps.setBigDecimal(5, v.consFees());
            ps.setBigDecimal(6, v.otherExpenses());
            ps.setString(7, v.id());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection con = Database.connect();
             PreparedStatement ps = con.prepareStatement("DELETE FROM PatientDetails WHERE PatientDetailsID=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Visit> query(String sql, Object parameter) throws SQLException {
        List<Visit> result = new ArrayList<>();
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (parameter != null) {
                ps.setObject(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Visit(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getInt(6), rs.getDate(7).toLocalDate(),
                            rs.getBigDecimal(8), rs.getBigDecimal(9)));
                }
            }
        }
        return result;
    }
}
