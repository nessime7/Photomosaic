package com.photomosaic.pixel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class PixelService {

    private static final int TILE_SIZE = 5;

    public void process() throws IOException {
        final var targetImage = ImageIO.read(new File("mainImage.jpg"));
        final var sourceDir = new File("source_images");
        final var sourceImages = getBufferedImages(sourceDir);

        final var mosaicImage = new BufferedImage(targetImage.getWidth(), targetImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        createPixelMosaic(targetImage, sourceImages, mosaicImage);
        savePhotomosaic(mosaicImage);
    }

    private void savePhotomosaic(BufferedImage mosaicImage) throws IOException {
        ImageIO.write(mosaicImage, "jpg", new File("PixelPhotomosaic.jpg"));
    }

    private void createPixelMosaic(BufferedImage targetImage, BufferedImage[] sourceImages, BufferedImage mosaicImage) {
        for (var x = 0; x < targetImage.getWidth(); x += TILE_SIZE) {
            for (var y = 0; y < targetImage.getHeight(); y += TILE_SIZE) {
                final var targetColor = new Color(targetImage.getRGB(x, y));
                final var imageScores = new ArrayList<ImageScore>(); // w docu prosze odpowiedziec dlaczego to dziala
                for (final var sourceImage : sourceImages) {
                    final var sourceColor = new Color(sourceImage.getRGB(0, 0));
                    final var distance = getColorDistance(targetColor, sourceColor);
                    imageScores.add(new ImageScore(distance, sourceImage));
                }
                imageScores.sort(Comparator.comparingInt(a -> a.score));
                final var closestImage = imageScores.get(0).image;
                for (var i = x; i < x + TILE_SIZE && i < targetImage.getWidth(); i++) {
                    for (var j = y; j < y + TILE_SIZE && j < targetImage.getHeight(); j++) {
                        mosaicImage.setRGB(i, j, closestImage.getRGB(0, 0));
                    }
                }
            }
        }
    }

    private BufferedImage[] getBufferedImages(File sourceDir) throws IOException {
        final var sourceFiles = sourceDir.listFiles((dir, name) -> name.endsWith(".jpg"));
        final var sourceImages = new BufferedImage[sourceFiles.length];
        for (var i = 0; i < sourceFiles.length; i++) {
            sourceImages[i] = ImageIO.read(sourceFiles[i]);
        }
        return sourceImages;
    }

    private static int getColorDistance(Color a, Color b) {
        final var deltaRed = a.getRed() - b.getRed();
        final var deltaGreen = a.getGreen() - b.getGreen();
        final var deltaBlue = a.getBlue() - b.getBlue();
        return deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue * deltaBlue;
    }
}