/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.misc;

import processing.core.PConstants;
import processing.core.PImage;

import java.awt.image.BufferedImage;

public enum ImageConverter {
	;

	public static PImage convBufferedImageToPImage(BufferedImage bimg, PImage img) {
        // code is inspired by https://forum.processing.org/one/topic/converting-bufferedimage-to-pimage.html

		if (img == null || img.width!=bimg.getWidth() || img.height != bimg.getHeight())
			img=new PImage(bimg.getWidth(),bimg.getHeight(), PConstants.ARGB);

        bimg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
        img.updatePixels();
        return img;
    }
}
