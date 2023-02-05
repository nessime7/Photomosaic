package com.PhotomosaicSara.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static com.PhotomosaicSara.image.Pixel.*;

public class MainPhotomosaic {

    private static final String TILES_DIRECTION = "source_images";
    private static final String INPUT_IMG = "mainImage.jpg";
    private static final String OUTPUT_IMG = "photomosaicImage.jpg";
    private static final int TILE_WIDTH = 90;
    private static final int TILE_HEIGHT = 90;
    private static final int TILE_SCALE = 9;
    private static final boolean IS_BW = false;

    public static void main(String[] args) throws IOException {
        final Collection<Tile> tileImages = getImagesFromTiles(new File(TILES_DIRECTION));

        File inputImageFile = new File(INPUT_IMG);
        Collection<BufferedImagePart> inputImageParts = getImagesFromInput(inputImageFile);
        final Collection<BufferedImagePart> outputImageParts = Collections.synchronizedSet(new HashSet<>());

        for (final BufferedImagePart inputImagePart : inputImageParts) {
            Tile bestFitTile = getBestFitTile(inputImagePart.image, tileImages);
            outputImageParts.add(new BufferedImagePart(bestFitTile.image, inputImagePart.x, inputImagePart.y));
        }

        BufferedImage inputImage = ImageIO.read(inputImageFile);
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage output = makeOutputImage(width, height, outputImageParts);
        ImageIO.write(output, "jpg", new File(OUTPUT_IMG));
    }

    private static BufferedImage makeOutputImage(int width, int height, Collection<BufferedImagePart> parts) {
        BufferedImage image = new BufferedImage(width * TILE_SCALE, height * TILE_SCALE, BufferedImage.TYPE_3BYTE_BGR);

        for (BufferedImagePart part : parts) {
            BufferedImage imagePart = image.getSubimage(part.x * TILE_SCALE, part.y * TILE_SCALE, TILE_WIDTH, TILE_HEIGHT);
            imagePart.setData(part.image.getData());
        }
        return image;
    }

    private static Tile getBestFitTile(BufferedImage target, Collection<Tile> tiles) {
        Tile bestFit = null;
        int bestFitScore = -1;

        for (Tile tile : tiles) {
            int score = getScore(target, tile);
            if (score > bestFitScore) {
                bestFitScore = score;
                bestFit = tile;
            }
        }
        return bestFit;
    }

    private static int getScore(BufferedImage target, Tile tile) {
        assert target.getHeight() == Tile.SCALED_HEIGHT;
        assert target.getWidth() == Tile.SCALED_WIDTH;

        int total = 0;
        for (int x = 0; x < Tile.SCALED_WIDTH; x++) {
            for (int y = 0; y < Tile.SCALED_HEIGHT; y++) {
                int targetPixel = target.getRGB(x, y);
                Pixel candidatePixel = tile.pixels[x][y];
                int diff = getDiff(targetPixel, candidatePixel);
                int score;
                if (IS_BW) {
                    score = 255 - diff;
                } else {
                    score = 255 * 3 - diff;
                }
                total += score;
            }
        }
        return total;
    }

    private static int getDiff(int target, Pixel candidate) {
        if (IS_BW) {
            return Math.abs(getRed(target) - candidate.r);
        } else {
            return Math.abs(getRed(target) - candidate.r) +
                    Math.abs(getGreen(target) - candidate.g) +
                    Math.abs(getBlue(target) - candidate.b);
        }
    }

    private static Collection<Tile> getImagesFromTiles(File tilesDir) throws IOException {
        Collection<Tile> tileImages = Collections.synchronizedSet(new HashSet<>());
        File[] files = tilesDir.listFiles();
        assert files != null;
        for (File file : files) {
            BufferedImage img = ImageIO.read(file);
            tileImages.add(new Tile(img));
        }
        return tileImages;
    }

    private static Collection<BufferedImagePart> getImagesFromInput(File inputImgFile) throws IOException {
        Collection<BufferedImagePart> parts = new HashSet<>();

        BufferedImage inputImage = ImageIO.read(inputImgFile);

        int totalHeight = inputImage.getHeight();
        int totalWidth = inputImage.getWidth();
        int x = 0;
        int y = 0;
        int w = Tile.SCALED_WIDTH;
        int h = Tile.SCALED_HEIGHT;
        while (x + w <= totalWidth) {
            while (y + h <= totalHeight) {
                BufferedImage inputImagePart = inputImage.getSubimage(x, y, w, h);
                parts.add(new BufferedImagePart(inputImagePart, x, y));
                y += h;
            }
            y = 0;
            x += w;
        }
        return parts;
    }

    public static class Tile {
        public static int SCALED_WIDTH = TILE_WIDTH / TILE_SCALE;
        public static int SCALED_HEIGHT = TILE_HEIGHT / TILE_SCALE;
        public Pixel[][] pixels = new Pixel[SCALED_WIDTH][SCALED_HEIGHT];
        public BufferedImage image;

        public Tile(BufferedImage image) {
            this.image = image;
            calcPixels();
        }

        private void calcPixels() {
            for (int x = 0; x < SCALED_WIDTH; x++) {
                for (int y = 0; y < SCALED_HEIGHT; y++) {
                    pixels[x][y] = calcPixel(x * TILE_SCALE, y * TILE_SCALE);
                }
            }
        }

        private Pixel calcPixel(int x, int y) {
            int redTotal = 0, greenTotal = 0, blueTotal = 0;

            for (int i = 0; i < MainPhotomosaic.TILE_SCALE; i++) {
                for (int j = 0; j < MainPhotomosaic.TILE_SCALE; j++) {
                    int rgb = image.getRGB(x + i, y + j);
                    redTotal += getRed(rgb);
                    greenTotal += getGreen(rgb);
                    blueTotal += getBlue(rgb);
                }
            }
            int count = MainPhotomosaic.TILE_SCALE * MainPhotomosaic.TILE_SCALE;
            return new Pixel(redTotal / count, greenTotal / count, blueTotal / count);
        }
    }
}
