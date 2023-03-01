package com.photomosaic.pixel;

import java.awt.image.BufferedImage;

public class ImageScore {

    public final int score;
    public final BufferedImage image;

    public ImageScore(int score, BufferedImage image) {
        this.score = score;
        this.image = image;
    }
}