package com.webscraper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class ImportManual {
    private static Logger logger = Logger.getLogger(ImportManual.class.getName());

    public static void main(String[] args) {
        // create folder fixed_images if not exist
        File folder = new File("fixed_images");
        if (!folder.exists()) {
            folder.mkdir();
        }
        // get all files in fixed_images folder
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    String fileName = file.getName();
                    String sid = fileName.substring(0, fileName.indexOf("."));
                    BufferedImage image;
                    image = ImageIO.read(file);
                    BufferedImage result = new BufferedImage(
                            image.getWidth(),
                            image.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
                    result.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
                    ImageIO.write(result, "jpg",
                            new File("images/" + sid + ".jpg"));
                    SQLiteJDBC.updateDownloaded(sid, 1, null);
                    // remove file
                    file.delete();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "error", e);
                }
            }
        }
    }
}
