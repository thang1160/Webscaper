package com.webscraper;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import java.awt.image.BufferedImage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Selenium {
    private static Logger logger = Logger.getLogger(Selenium.class.getName());

    public static int downloadImage(String url, String sid, boolean panoOnly) throws InterruptedException {
        int downloaded = 0;
        WebDriver driver = new ChromeDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
        // Thread.sleep(2000);
        String actualUrl = driver.getCurrentUrl();
        // prevent looping
        if (!actualUrl.equals(url))
            return 0;
        WebElement fileList = driver.findElement(By.id("fileList"));
        List<WebElement> rows = fileList.findElements(By.tagName("tr"));
        for (WebElement row : rows) {
            String dataMime = row.getAttribute("data-mime");
            if (dataMime.toLowerCase().contains("image")) {
                String fileName = row.getAttribute("data-file");
                String imageUrl = "https://xq.ane.vn/s/" + sid + "/download?path="
                        + encodeValue(row.getAttribute("data-path"))
                        + "&files="
                        + encodeValue(fileName);
                if (!imageUrl.toLowerCase().contains("pano") && panoOnly)
                    continue;
                logger.info("downloading image: " + imageUrl);
                try {
                    URL website = new URL(imageUrl);
                    try (InputStream in = website.openStream()) {
                        BufferedImage image = ImageIO.read(in);
                        BufferedImage result = new BufferedImage(
                                image.getWidth(),
                                image.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                        result.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
                        ImageIO.write(result, "jpg",
                                new File("images/" + sid + ".jpg"));
                    }
                    downloaded++;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "error", e);
                }
            } else if (dataMime.toLowerCase().contains("http")) {
                WebElement anchor = row.findElement(By.cssSelector("a.name"));
                String href = anchor.getAttribute("href");
                downloaded += downloadImage(href, sid, panoOnly);
            }
        }
        driver.quit();
        return downloaded;
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
        String imageUrl = "https://xq.ane.vn/s/kxjzyADpaAFdCeL/download?path=%2FPHIM-G%E1%BB%90C&files=BUI+TUAN+NGOC.bmp";
        logger.info("downloading image: " + imageUrl);
        try {
            URL website = new URL(imageUrl);
            HttpsURLConnection con = (HttpsURLConnection)website.openConnection();
            try (InputStream in = con.getInputStream()) {
                BufferedImage image = ImageIO.read(in);
                BufferedImage result = new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                result.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
                ImageIO.write(result, "jpg",
                        new File("images/kxjzyADpaAFdCeL.jpg"));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error", e);
        }
    }
}
