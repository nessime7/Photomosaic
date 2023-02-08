package com.PhotomosaicSara.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static com.PhotomosaicSara.image.Pixel.*;

public class PhotomosaicService {

    private static final int TILE_WIDTH = 90;
    private static final int TILE_HEIGHT = 90;
    private static final int TILE_SCALE = 9;

    public void process() throws IOException {
        final var inputImageFile = new File("mainImage.jpg");
        final var inputImageParts = getImagesFromInput(inputImageFile);

        final var tileImages = getImagesFromTiles(new File("source_images"));
        final ArrayList<BufferedImagePart> outputImageParts = new ArrayList<>();

        createOutput(inputImageParts, tileImages, outputImageParts);
        getPhotomozaic(inputImageFile, outputImageParts);
    }

    public BufferedImage makeOutputImage(int width, int height, Collection<BufferedImagePart> parts) {
        final var image = new BufferedImage(width * TILE_SCALE, height * TILE_SCALE, BufferedImage.TYPE_3BYTE_BGR);

        for (final var part : parts) {
            final var imagePart = image.getSubimage(part.x * TILE_SCALE, part.y * TILE_SCALE, TILE_WIDTH, TILE_HEIGHT);
            imagePart.setData(part.image.getData());
        }
        return image;
    }

    public Tile getBestFitTile(BufferedImage target, Collection<Tile> tiles) {
        Tile bestFit = null;
        int bestFitScore = -1;

        for (final var tile : tiles) {
            final var score = getScore(target, tile);
            if (score > bestFitScore) {
                bestFitScore = score;
                bestFit = tile;
            }
        }
        return bestFit;
    }

    private int getScore(BufferedImage target, Tile tile) {
        final var height = target.getHeight();
        final var width = target.getWidth();

        if (height != Tile.scaledHeight){
            throw new AssertionError("Unexpected height: " + height);
        }

        if (width != Tile.scaledWidth){
            throw new AssertionError("Unexpected width: " + height);
        }

        int total = 0;
        for (int x = 0; x < Tile.scaledWidth; x++) {
            for (int y = 0; y < Tile.scaledHeight; y++) {
                final var targetPixel = target.getRGB(x, y);
                final var candidatePixel = tile.pixels[x][y];
                final var diff = getDifference(targetPixel, candidatePixel);
                int score;
                score = 255 * 3 - diff;
                total += score;
            }
        }
        return total;
    }

    private int getDifference(int target, Pixel candidate) {
        return Math.abs(getRed(target) - candidate.r) +
                Math.abs(getGreen(target) - candidate.g) +
                Math.abs(getBlue(target) - candidate.b);
    }

    public Collection<Tile> getImagesFromTiles(File tilesDir) throws IOException {
        Collection<Tile> tileImages = Collections.synchronizedSet(new HashSet<>());
        final var files = tilesDir.listFiles();
        assert files != null;
        for (final var file : files) {
            final var img = ImageIO.read(file);
            tileImages.add(new Tile(img));
        }
        return tileImages;
    }

    public Collection<BufferedImagePart> getImagesFromInput(File inputImgFile) throws IOException {
        Collection<BufferedImagePart> parts = new HashSet<>();
        final var inputImage = ImageIO.read(inputImgFile);
        final var totalHeight = inputImage.getHeight();
        final var totalWidth = inputImage.getWidth();
        int x = 0;
        int y = 0;
        int w = Tile.scaledWidth;
        int h = Tile.scaledHeight;
        while (x + w <= totalWidth) {
            while (y + h <= totalHeight) {
                final var inputImagePart = inputImage.getSubimage(x, y, w, h);
                parts.add(new BufferedImagePart(inputImagePart, x, y));
                y += h;
            }
            y = 0;
            x += w;
        }
        return parts;
    }

    public void createOutput(Collection<BufferedImagePart> inputImageParts, Collection<Tile> tileImages, ArrayList<BufferedImagePart> outputImageParts) {
        for (final var inputImagePart : inputImageParts) {
            final var bestFitTile = getBestFitTile(inputImagePart.image, tileImages);
            outputImageParts.add(new BufferedImagePart(bestFitTile.image, inputImagePart.x, inputImagePart.y));
        }
    }

    public void getPhotomozaic(File inputImageFile, ArrayList<BufferedImagePart> outputImageParts) throws IOException {
        final var inputImage = ImageIO.read(inputImageFile);
        final var width = inputImage.getWidth();
        final var height = inputImage.getHeight();
        final var output = makeOutputImage(width, height, outputImageParts);
        ImageIO.write(output, "jpg", new File("photomosaicImage.jpg"));
    }
}
