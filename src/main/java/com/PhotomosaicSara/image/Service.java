package com.PhotomosaicSara.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static com.PhotomosaicSara.image.Pixel.*;

public class Service {

    private static final int TILE_WIDTH = 90;
    private static final int TILE_HEIGHT = 90;
    private static final int TILE_SCALE = 9;

    public BufferedImage makeOutputImage(int width, int height, Collection<BufferedImagePart> parts) {
        final var image = new BufferedImage(width * TILE_SCALE, height * TILE_SCALE, BufferedImage.TYPE_3BYTE_BGR);

        for (BufferedImagePart part : parts) {
            final var imagePart = image.getSubimage(part.x * TILE_SCALE, part.y * TILE_SCALE, TILE_WIDTH, TILE_HEIGHT);
            imagePart.setData(part.image.getData());
        }
        return image;
    }

    public Tile getBestFitTile(BufferedImage target, Collection<Tile> tiles) {
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

    private int getScore(BufferedImage target, Tile tile) {
        assert target.getHeight() == Tile.scaledHeight;
        assert target.getWidth() == Tile.scaledWidth;

        int total = 0;
        for (int x = 0; x < Tile.scaledWidth; x++) {
            for (int y = 0; y < Tile.scaledHeight; y++) {
                int targetPixel = target.getRGB(x, y);
                Pixel candidatePixel = tile.pixels[x][y];
                int diff = getDiff(targetPixel, candidatePixel);
                int score;
                score = 255 * 3 - diff;
                total += score;
            }
        }
        return total;
    }

    private int getDiff(int target, Pixel candidate) {
        return Math.abs(getRed(target) - candidate.r) +
                Math.abs(getGreen(target) - candidate.g) +
                Math.abs(getBlue(target) - candidate.b);
    }

    public Collection<Tile> getImagesFromTiles(File tilesDir) throws IOException {
        Collection<Tile> tileImages = Collections.synchronizedSet(new HashSet<>());
        final var files = tilesDir.listFiles();
        assert files != null;
        for (File file : files) {
            BufferedImage img = ImageIO.read(file);
            tileImages.add(new Tile(img));
        }
        return tileImages;
    }

    public Collection<BufferedImagePart> getImagesFromInput(File inputImgFile) throws IOException {

        Collection<BufferedImagePart> parts = new HashSet<>();
        final var inputImage = ImageIO.read(inputImgFile);

        int totalHeight = inputImage.getHeight();
        int totalWidth = inputImage.getWidth();
        int x = 0;
        int y = 0;
        int w = Tile.scaledWidth;
        int h = Tile.scaledHeight;
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

}
