package com.ontime.mediconnect.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens connections using the settings in db.properties. */
public final class Database {
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in == null) throw new IOException("db.properties not found on classpath");
            CONFIG.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Database() { }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(CONFIG.getProperty("url"),
                CONFIG.getProperty("user"), CONFIG.getProperty("password"));
    }
}
