package com.PhotomosaicSara.image;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Photomosaic {

    private final Service service;

    public Photomosaic(Service service) {
        this.service = service;
    }

    public void process() throws IOException {

        final var tileImages = service.getImagesFromTiles(new File("source_images"));
        final var inputImageFile = new File("mainImage.jpg");
        final var inputImageParts = service.getImagesFromInput(inputImageFile);
        final Collection<BufferedImagePart> outputImageParts = Collections.synchronizedSet(new HashSet<>());

        for (final BufferedImagePart inputImagePart : inputImageParts) {
            final var bestFitTile = service.getBestFitTile(inputImagePart.image, tileImages);
            outputImageParts.add(new BufferedImagePart(bestFitTile.image, inputImagePart.x, inputImagePart.y));
        }

        final var inputImage = ImageIO.read(inputImageFile);
        final var width = inputImage.getWidth();
        final var height = inputImage.getHeight();
        final var output = service.makeOutputImage(width, height, outputImageParts);
        ImageIO.write(output, "jpg", new File("photomosaicImage.jpg"));
    }
}
