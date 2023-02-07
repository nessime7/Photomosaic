package com.PhotomosaicSara.image;

public class Pixel {

        public final int r;
        public final int g;
        public final int b;

        public Pixel(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

    public static int getRed(int pixel) {
        return (pixel >>> 16) & 0xff;
    }

    public static int getGreen(int pixel) {
        return (pixel >>> 8) & 0xff;
    }

    public static int getBlue(int pixel) {
        return pixel & 0xff;
    }
    }
