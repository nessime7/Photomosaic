package com.PhotomosaicSara.image;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Photomosaic {

    private final PhotomosaicService photomosaicService;

    public Photomosaic(PhotomosaicService photomosaicService) {
        this.photomosaicService = photomosaicService;
    }

    public void process() throws IOException {
        final var tileImages = photomosaicService.getImagesFromTiles(new File("source_images"));
        final var inputImageFile = new File("mainImage.jpg");
        final var inputImageParts = photomosaicService.getImagesFromInput(inputImageFile);
        final ArrayList<BufferedImagePart> outputImageParts = new ArrayList<>();

        for (final var inputImagePart : inputImageParts) {
            final var bestFitTile = photomosaicService.getBestFitTile(inputImagePart.image, tileImages);
            outputImageParts.add(new BufferedImagePart(bestFitTile.image, inputImagePart.x, inputImagePart.y));
        }

        final var inputImage = ImageIO.read(inputImageFile);
        final var width = inputImage.getWidth();
        final var height = inputImage.getHeight();
        final var output = photomosaicService.makeOutputImage(width, height, outputImageParts);
        ImageIO.write(output, "jpg", new File("photomosaicImage.jpg"));
    }
}
