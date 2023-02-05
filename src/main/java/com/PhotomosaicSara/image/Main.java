package com.PhotomosaicSara.image;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new Photomosaic(new Service()).process();
    }

}
