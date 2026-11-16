package com.ontime.mediconnect.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/** Simple count queries used by the Queries screen. */
public class CountDao {

    public int doctors() throws SQLException {
        return scalar("SELECT COUNT(*) FROM Doctor");
    }

    public int patients() throws SQLException {
        return scalar("SELECT COUNT(*) FROM Patient");
    }

    /** Number of different patients who visited on the given date. */
    public int patientsOn(LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT PatientID) FROM PatientDetails WHERE CheckupDate = ?";
        try (Connection con = Database.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int scalar(String sql) throws SQLException {
        try (Connection con = Database.connect(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
