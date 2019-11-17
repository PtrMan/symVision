/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.InternalTests;

public enum TestVisualProcessor1
{
	;

	public static void main(String[] args)
    {
        /* outdated

        Vector2d<Integer> imageSize;
        IMap2d<ColorRgb> inputMap;
        IMap2d<Boolean> zeroCrossingsMap;
        BufferedImage resultImage;

        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("/home/r0b3/flowerMini.png"));
        } catch (IOException e) {
            int x = 0;
        }

        imageSize = new Vector2d<>(img.getWidth(), img.getHeight());

        VisualProcessor.ProcessingChain processingChain;

        processingChain = new VisualProcessor.ProcessingChain();
        processingChain.setup(imageSize, new ColorRgb(1.0f, 1.0f, 1.0f), new VisualProcessor.ProcessingChain.MarrHildrethOperatorParameter(10, 0.25f));

        inputMap = Map2dImageConverter.convertImageToMap(img);

        processingChain.filterChain(inputMap);

        zeroCrossingsMap = processingChain.getZeroCrossingBinary();
        resultImage = Map2dImageConverter.convertBooleanMapToImage(zeroCrossingsMap);

        File f = new File("/home/r0b3/MyFile.png");
        try {
            ImageIO.write(resultImage, "PNG", f);
        }
        catch(IOException e) {
        }
        */
    }
}
