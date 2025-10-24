package com.example.qrcode;

import com.example.task.Task;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class QRCodeGenerator {

    public static void showQrCodeBase64(Task task) {
        String qrContent = "Task ID: " + task.getId() + " - " + task.getDescription();
        String base64Image = "";

        try {
            // Lógica de geração do QR Code com ZXing
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

            // Geração da imagem como BufferedImage (do pacote java.awt.image)
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            // Conversão para Base64 (requer javax.imageio.ImageIO)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (Exception e) {
            System.err.println("Erro ao gerar QR Code: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }

        // Cria o componente Image usando a string Base64 (que não requer StreamResource)
        String imageSource = "data:image/png;base64," + base64Image;
        Image qrImage = new Image(imageSource, "QR Code for Task " + task.getDescription());
        qrImage.setWidth("200px");
        qrImage.setHeight("200px");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("QR Code para a Tarefa: " + task.getDescription());
        dialog.add(qrImage);
        dialog.open();
    }

}
