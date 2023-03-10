package com.photomosaic.image;

import java.awt.image.BufferedImage;

public class BufferedImagePart {

    public final BufferedImage image;
    public final int x;
    public final int y;

    public BufferedImagePart(BufferedImage image, int x, int y) {
        this.image = image;
        this.x = x;
        this.y = y;
    }
}
