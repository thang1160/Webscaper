package com.webscraper;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App {
    private static Logger logger = Logger.getLogger(App.class.getName());

    static {
        // create folder images & fixed_images if not exist
        File folder = new File("images");
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File("fixed_images");
        if (!folder.exists()) {
            folder.mkdir();
        }
        String absolutePath = null;
        // Check current operating system
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            absolutePath = new File("chromedriver/windows.exe").getAbsolutePath();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            absolutePath = new File("chromedriver/linux").getAbsolutePath();
        } else {
            logger.log(Level.SEVERE, "Unsupported OS");
            System.exit(1);
        }
        System.setProperty("webdriver.chrome.driver", absolutePath);
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException, SQLException {
        List<List<Object>> values = GoogleSheets.getList(0, 7400);
        for (List<Object> list : values) {
            String url = list.get(1).toString().trim();
            String sid = url.substring(url.lastIndexOf("/") + 1);
            logger.info("checking url: " + url);
            try {
                int downloaded = SQLiteJDBC.getDownloaded(url);
                if (downloaded > 0) {
                    logger.info("already downloaded: " + url);
                    continue;
                } else if (downloaded == 0) {
                    File folder = new File("fixed_images/" + sid);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    Selenium.downloadAllImage(url, sid, 0);
                    continue;
                }
                SQLiteJDBC.insertImage(sid, url);
                if (list.get(0).toString().contains("+"))
                    downloaded = Selenium.downloadImage(url, sid, true, 0);
                else
                    downloaded = Selenium.downloadImage(url, sid, false, 0);
                SQLiteJDBC.updateDownloaded(sid, downloaded, null);
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "error", e);
                SQLiteJDBC.updateDownloaded(sid, 1, e.getMessage());
            }
        }
    }
}
