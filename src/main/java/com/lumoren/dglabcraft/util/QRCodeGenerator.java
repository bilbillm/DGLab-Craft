package com.lumoren.dglabcraft.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 将 DGLab 连接 URL 生成二维码图片保存到本地
 */
public class QRCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("QRCodeGenerator");

    private static final int QR_SIZE = 400;
    private static final String QR_FILE_NAME = "dglab-qrcode.png";

    private static File qrCodeFile = null;

    /**
     * 生成二维码图片
     * @param content DGLab 连接 URL
     * @return 生成的图片文件对象
     */
    public static File generateQRCode(String content) {
        try {
            // 创建 QR 码写入器
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // 设置编码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            // 生成 BitMatrix
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

            // 创建图片
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);

            // 绘制二维码图案
            graphics.setColor(Color.BLACK);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();

            // 获取游戏运行目录
            Minecraft mc = Minecraft.getInstance();
            File gameDir = mc.gameDirectory;

            // 保存图片
            qrCodeFile = new File(gameDir, QR_FILE_NAME);
            ImageIO.write(image, "png", qrCodeFile);

            LOGGER.info("二维码图片已生成: " + qrCodeFile.getAbsolutePath());
            return qrCodeFile;

        } catch (WriterException | IOException e) {
            LOGGER.error("生成二维码图片失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取二维码图片文件
     * @return 二维码图片文件，如果不存在则返回 null
     */
    public static File getQrCodeFile() {
        return qrCodeFile;
    }
}
