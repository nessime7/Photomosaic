package com.PhotomosaicSara.image;

import java.awt.image.BufferedImage;

import static com.PhotomosaicSara.image.Pixel.*;

public class Tile {

    private static final int tileWidth = 90;
    private static final int tileHeight = 90;
    private static final int tileScale = 9;
    public static int scaledWidth = tileWidth / tileScale;
    public static int scaledHeight = tileHeight / tileScale;
    public Pixel[][] pixels = new Pixel[scaledWidth][scaledHeight];
    public BufferedImage image;

    public Tile(BufferedImage image) {
        this.image = image;
        calcPixels();
    }

    private void calcPixels() {
        for (int x = 0; x < scaledWidth; x++) {
            for (int y = 0; y < scaledHeight; y++) {
                pixels[x][y] = calcPixel(x * tileScale, y * tileScale);
            }
        }
    }

    private Pixel calcPixel(int x, int y) {
        int redTotal = 0, greenTotal = 0, blueTotal = 0;

        for (int i = 0; i < tileScale; i++) {
            for (int j = 0; j < tileScale; j++) {
                int rgb = image.getRGB(x + i, y + j);
                redTotal += getRed(rgb);
                greenTotal += getGreen(rgb);
                blueTotal += getBlue(rgb);
            }
        }
        int count = tileScale * tileScale;
        return new Pixel(redTotal / count, greenTotal / count, blueTotal / count);
    }

}
