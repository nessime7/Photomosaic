package com.photomosaic.image;

import java.awt.image.BufferedImage;

import static com.photomosaic.image.Pixel.*;

public class Tile {

    private static final int TILE_WIDTH = 90;
    private static final int TILE_HEIGHT = 90;
    private static final int TILE_SCALE = 9;
    public static int SCALED_WIDTH = TILE_WIDTH / TILE_SCALE;
    public static int SCALED_HEIGHT = TILE_HEIGHT / TILE_SCALE;

    public Pixel[][] pixels = new Pixel[SCALED_WIDTH][SCALED_HEIGHT];
    public BufferedImage image;

    public Tile(BufferedImage image) {
        this.image = image;
        calcPixels();
    }

    private void calcPixels() {
        for (var x = 0; x < SCALED_WIDTH; x++) {
            for (var y = 0; y < SCALED_HEIGHT; y++) {
                pixels[x][y] = calcPixel(x * TILE_SCALE, y * TILE_SCALE);
            }
        }
    }

    private Pixel calcPixel(int x, int y) {
        int redTotal = 0, greenTotal = 0, blueTotal = 0;

        for (var i = 0; i < TILE_SCALE; i++) {
            for (var j = 0; j < TILE_SCALE; j++) {
                final var rgb = image.getRGB(x + i, y + j);
                redTotal += getRed(rgb);
                greenTotal += getGreen(rgb);
                blueTotal += getBlue(rgb);
            }
        }
        final var count = TILE_SCALE * TILE_SCALE;
        return new Pixel(redTotal / count, greenTotal / count, blueTotal / count);
    }
}
