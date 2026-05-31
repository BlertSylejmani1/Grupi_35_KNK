package com.smartinventory.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.smartinventory.model.Product;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;

public final class QrCodeService {
    private static final int SIZE = 260;

    private QrCodeService() {
    }

    public static Image forProduct(Product product) {
        try {
            return toImage(matrixFor(payload(product)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate QR code", ex);
        }
    }

    public static String payload(Product product) {
        return """
                SMART INVENTORY PRODUCT
                Product ID: %d
                Name: %s
                Category: %s
                Supplier: %s
                Price: %s
                Stock: %d
                Status: %s
                """.formatted(
                product.getId(),
                safe(product.getName()),
                safe(product.getCategory()),
                safe(product.getSupplier()),
                product.getPrice().toPlainString(),
                product.getQuantity(),
                safe(product.getStatus())
        );
    }

    private static BitMatrix matrixFor(String content) throws Exception {
        return new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, SIZE, SIZE);
    }

    private static Image toImage(BitMatrix matrix) {
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
        WritableImage image = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                image.getPixelWriter().setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return image;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
