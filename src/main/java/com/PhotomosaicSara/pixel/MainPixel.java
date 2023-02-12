package com.PhotomosaicSara.pixel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainPixel {

    private static final int TILE_SIZE = 5;

    public static void main(String[] args) throws IOException {

        final var targetImage = ImageIO.read(new File("mainImage.jpg"));
        final var sourceDir = new File("source_images");
        final var sourceFiles = sourceDir.listFiles((dir, name) -> name.endsWith(".jpg"));
        final var sourceImages = new BufferedImage[sourceFiles.length];
        for (int i = 0; i < sourceFiles.length; i++) {
            sourceImages[i] = ImageIO.read(sourceFiles[i]);
        }

        final var mosaicImage = new BufferedImage(targetImage.getWidth(), targetImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < targetImage.getWidth(); x += TILE_SIZE) {
            for (int y = 0; y < targetImage.getHeight(); y += TILE_SIZE) {
                final var targetColor = new Color(targetImage.getRGB(x, y));
                List<ImageScore> imageScores = new ArrayList<>();
                for (final var sourceImage : sourceImages) {
                    final var sourceColor = new Color(sourceImage.getRGB(0, 0));
                    final var distance = getColorDistance(targetColor, sourceColor);
                    imageScores.add(new ImageScore(distance, sourceImage));
                }
                imageScores.sort(Comparator.comparingInt(a -> a.score));
                final var closestImage = imageScores.get(0).image;
                for (int i = x; i < x + TILE_SIZE && i < targetImage.getWidth(); i++) {
                    for (int j = y; j < y + TILE_SIZE && j < targetImage.getHeight(); j++) {
                        mosaicImage.setRGB(i, j, closestImage.getRGB(0, 0));
                    }
                }
            }
        }

        ImageIO.write(mosaicImage, "jpg", new File("PixelPhotomosaic.jpg"));
    }

    private static int getColorDistance(Color a, Color b) {
        final var deltaRed = a.getRed() - b.getRed();
        final var deltaGreen = a.getGreen() - b.getGreen();
        final var deltaBlue = a.getBlue() - b.getBlue();
        return deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue * deltaBlue;
    }
}