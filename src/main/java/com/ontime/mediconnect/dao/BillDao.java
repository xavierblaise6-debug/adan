package com.ontime.mediconnect.dao;

import com.ontime.mediconnect.model.Bill;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Database access for the Bill table. */
public class BillDao {

    /** Creates the bill for a visit; the amount is calculated in the database from the visit's charges. */
    public Bill generate(String visitId, String modeOfPayment) throws SQLException {
        String sql = "INSERT INTO Bill (PatientDetailsID, PatientID, BillAmount, ModeOfPayment) "
                + "SELECT PatientDetailsID, PatientID, ConsFees + OtherExpenses, ? "
                + "FROM PatientDetails WHERE PatientDetailsID = ?";
        try (Connection con = Database.connect();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, modeOfPayment);
            ps.setString(2, visitId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Visit " + visitId + " was not found.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return findById(con, keys.getInt(1));
            }
        }
    }

    public List<Bill> findByPatient(String patientId) throws SQLException {
        String sql = "SELECT BillID, PatientDetailsID, PatientID, BillDate, BillAmount, ModeOfPayment "
                + "FROM Bill WHERE PatientID = ? ORDER BY BillID";
        List<Bill> result = new ArrayList<>();
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    /** Total billed on one calendar day. */
    public BigDecimal revenueOn(java.time.LocalDate date) throws SQLException {
        String sql = "SELECT COALESCE(SUM(BillAmount), 0) FROM Bill WHERE CAST(BillDate AS DATE) = ?";
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private Bill findById(Connection con, int id) throws SQLException {
        String sql = "SELECT BillID, PatientDetailsID, PatientID, BillDate, BillAmount, ModeOfPayment "
                + "FROM Bill WHERE BillID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    private static Bill map(ResultSet rs) throws SQLException {
        return new Bill(rs.getInt(1), rs.getString(2), rs.getString(3),
                rs.getTimestamp(4).toLocalDateTime(), rs.getBigDecimal(5), rs.getString(6));
    }
}
