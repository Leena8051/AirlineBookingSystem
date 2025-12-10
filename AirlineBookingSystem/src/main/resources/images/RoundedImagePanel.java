/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.airlinebookingsystem.resources.images;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RoundedImagePanel extends JPanel {

    private BufferedImage image;

    public RoundedImagePanel(String imagePath) {
        try {
            image = ImageIO.read(getClass().getResource(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            Graphics2D g2 = (Graphics2D) g.create();

            int width = getWidth();
            int height = getHeight();

            // تنعيم الحواف
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Float(0, 0, width, height, 50, 50);
            g2.setClip(clip);

            g2.drawImage(image, 0, 0, width, height, this);
            g2.dispose();
        }
    }
}
