package com.webscraper;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App {
    private static Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // create folder images if not exist
        File folder = new File("images");
        if (!folder.exists()) {
            folder.mkdir();
        }
        String absolutePath = new File("chromedriver.exe").getAbsolutePath();
        System.setProperty("webdriver.chrome.driver", absolutePath);
        List<List<Object>> values = GoogleSheets.getList(2000, 3000);
        for (List<Object> list : values) {
            String url = list.get(1).toString().trim();
            logger.info("checking url: " + url);
            int downloaded = SQLiteJDBC.getDownloaded(url);
            if (downloaded > 0) {
                logger.info("already downloaded: " + url);
                continue;
            }
            String sid = url.substring(url.lastIndexOf("/") + 1);
            try {
                if (downloaded == -1)
                    SQLiteJDBC.insertImage(sid, url);
                if (list.get(0).toString().contains("+"))
                    downloaded = Selenium.downloadImage(url, sid, true);
                else
                    downloaded = Selenium.downloadImage(url, sid, false);
                SQLiteJDBC.updateDownloaded(sid, downloaded, null);
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "error", e);
                SQLiteJDBC.updateDownloaded(sid, 1, e.getMessage());
            }
        }
    }
}
