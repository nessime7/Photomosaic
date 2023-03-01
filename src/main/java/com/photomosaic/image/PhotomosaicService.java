package com.photomosaic.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static com.photomosaic.image.Pixel.*;
import static com.photomosaic.image.Tile.SCALED_HEIGHT;

public class PhotomosaicService {

    private static final String PHOTOMOSAIC_FILE_PATH = "photomosaicImage.jpg";
    private static final String PHOTOMOSAIC_FILE_FORMAT = "jpg";
    private static final String IMAGE_FILE_PATH = "mainImage.jpg";
    private static final String SOURCE_IMAGES_PATH = "source_images";

    private static final int TILE_WIDTH = 90;
    private static final int TILE_HEIGHT = 90;
    private static final int TILE_SCALE = 9;
    public static final int SCALED_WIDTH = Tile.SCALED_WIDTH;

    public void process() throws IOException {
        final var inputImageFile = new File(IMAGE_FILE_PATH);
        final var inputImageParts = getImagesFromInput(inputImageFile);

        final var tileImages = getImagesFromTiles(new File(SOURCE_IMAGES_PATH));
        final var outputImageParts = new ArrayList<BufferedImagePart>();

        createOutput(inputImageParts, tileImages, outputImageParts);
        createPhotomozaic(inputImageFile, outputImageParts);
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
        var bestFitScore = -1;

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

        if (height != SCALED_HEIGHT) {
            throw new AssertionError("Unexpected height: " + height);
        }

        if (width != Tile.SCALED_WIDTH) {
            throw new AssertionError("Unexpected width: " + height);
        }

        var total = 0;
        for (var x = 0; x < Tile.SCALED_WIDTH; x++) {
            for (var y = 0; y < SCALED_HEIGHT; y++) {
                final var targetPixel = target.getRGB(x, y);
                final var candidatePixel = tile.pixels[x][y];
                final var diff = getDifference(targetPixel, candidatePixel);
                final var score = 255 * 3 - diff;
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
        final var tileImages = new ArrayList<Tile>();
        final var files = tilesDir.listFiles();
        for (final var file : files) {
            final var img = ImageIO.read(file);
            tileImages.add(new Tile(img));
        }
        return tileImages;
    }

    public Collection<BufferedImagePart> getImagesFromInput(File inputImgFile) throws IOException {
        final var parts = new HashSet<BufferedImagePart>();
        final var inputImage = ImageIO.read(inputImgFile);
        final var totalHeight = inputImage.getHeight();
        final var totalWidth = inputImage.getWidth();
        var x = 0;
        var y = 0;
        while (x + SCALED_WIDTH <= totalWidth) {
            while (y + SCALED_HEIGHT <= totalHeight) {
                final var inputImagePart = inputImage.getSubimage(x, y, SCALED_WIDTH, SCALED_HEIGHT);
                parts.add(new BufferedImagePart(inputImagePart, x, y));
                y += SCALED_HEIGHT;
            }
            y = 0;
            x += SCALED_WIDTH;
        }
        return parts;
    }

    public void createOutput(Collection<BufferedImagePart> inputImageParts, Collection<Tile> tileImages, ArrayList<BufferedImagePart> outputImageParts) {
        for (final var inputImagePart : inputImageParts) {
            final var bestFitTile = getBestFitTile(inputImagePart.image, tileImages);
            outputImageParts.add(new BufferedImagePart(bestFitTile.image, inputImagePart.x, inputImagePart.y));
        }
    }

    public void createPhotomozaic(File inputImageFile, ArrayList<BufferedImagePart> outputImageParts) throws IOException {
        final var inputImage = ImageIO.read(inputImageFile);
        final var width = inputImage.getWidth();
        final var height = inputImage.getHeight();
        final var output = makeOutputImage(width, height, outputImageParts);
        ImageIO.write(output, PHOTOMOSAIC_FILE_FORMAT, new File(PHOTOMOSAIC_FILE_PATH));
    }
}
