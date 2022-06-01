package com.webscraper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteJDBC {
    private static Logger logger = Logger.getLogger(SQLiteJDBC.class.getName());
    private static Connection c = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:webscraper.db");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error", e);
            System.exit(0);
        }
        logger.info("Operation done successfully");
    }

    // -1: haven't scrape, 0: scraped but download failed/file not exist, >1: downloaded
    public static int getDownloaded(String url) {
        String query = "SELECT downloaded FROM image WHERE url=?;";
        try (PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, url);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("downloaded");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error", e);
        }
        return -1;
    }

    public static void insertImage(String sid, String url) {
        String query = "INSERT INTO image (sid, url) VALUES (?, ?);";
        try (PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, sid);
            ps.setString(2, url);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error", e);
        }
    }

    public static void updateDownloaded(String sid, int downloaded, String error) {
        String query = "UPDATE image SET downloaded=?, error=? WHERE sid=?;";
        try (PreparedStatement ps = c.prepareStatement(query)) {
            ps.setInt(1, downloaded);
            ps.setString(2, error);
            ps.setString(3, sid);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error", e);
        }
    }
}
