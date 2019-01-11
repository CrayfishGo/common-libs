package common.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Date: 2016/9/8
 */

public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * base64压缩
     *
     * @param base64
     * @param w
     * @param h
     * @return
     */
    public static String compressBase64(String base64, int w, int h) {
        String base64Str = null;
        File srcImageFile = null;
        File destImageFile = null;
        try {
            String temp = System.getProperty("java.io.tmpdir") + "/temp/compress/";
            File tempdir = new File(temp);
            if (!tempdir.exists()) {
                tempdir.mkdirs();
            }
            String uuid = UUID.randomUUID().toString();
            //源图片
            String srcImage = temp + uuid + "_src.jpg";
            srcImageFile = new File(srcImage);
            FileOutputStream srcOut = new FileOutputStream(srcImageFile);
            srcOut.write(decodeImage(base64));
            srcOut.close();
            //压缩后的图片
            String destImage = temp + uuid + "_dest.jpg";
            destImageFile = new File(destImage);
            Thumbnails.of(srcImageFile).size(w, h).toFile(destImageFile);
            BufferedImage img1 = ImageIO.read(destImageFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img1, "jpg", baos);
            byte[] base64Byte = baos.toByteArray();
            base64Str = Base64Utils.encodeToString(base64Byte);
        } catch (IOException e) {
            LOGGER.error("压缩图片出现异常：" + e.getMessage());
        } finally {
            try {
                if (srcImageFile != null) {
                    FileUtils.forceDelete(srcImageFile);
                }
                if (destImageFile != null) {
                    FileUtils.forceDelete(destImageFile);
                }
            } catch (IOException e) {
                LOGGER.error("压缩图片后删除文件出现异常：" + e.getMessage());
            }
        }
        return base64Str;
    }

    /**
     * @param imageDataString
     * @return
     */
    public static byte[] decodeImage(String imageDataString) {
        return Base64.decodeBase64(imageDataString);
    }

    /**
     * @param img1
     * @param img2
     * @return
     */
    public static BufferedImage twoImagesIntoOne(BufferedImage img1, BufferedImage img2) {
        int offset = 20;
        int width = Math.max(img1.getWidth(), img2.getWidth());
        int height = img1.getHeight() + img2.getHeight() + offset;
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, width, height);
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, 0, img1.getHeight() + offset);
        g2.dispose();
        return newImage;
    }


}
