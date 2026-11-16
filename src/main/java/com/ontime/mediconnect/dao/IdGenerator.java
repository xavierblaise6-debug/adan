package com.ontime.mediconnect.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Produces the next 4-character ID such as D001, P002, V003 for a table. */
final class IdGenerator {
    private IdGenerator() { }

    /**
     * @param table  table name (a trusted constant, never user input)
     * @param column ID column name (a trusted constant)
     * @param prefix one letter, e.g. "D"
     */
    static String next(Connection con, String table, String column, String prefix) throws SQLException {
        String sql = "SELECT MAX(" + column + ") FROM " + table;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            String max = rs.getString(1);
            int number = max == null ? 1 : Integer.parseInt(max.substring(1)) + 1;
            if (number > 999) {
                throw new SQLException("ID range exhausted for table " + table);
            }
            return prefix + String.format("%03d", number);
        }
    }
}
