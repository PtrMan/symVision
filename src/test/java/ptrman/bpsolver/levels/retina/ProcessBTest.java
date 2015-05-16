package ptrman.bpsolver.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.Test;
import ptrman.Datastructures.*;
import ptrman.levels.retina.ProcessA;
import ptrman.levels.retina.ProcessB;
import ptrman.levels.visual.ColorRgb;
import ptrman.levels.visual.VisualProcessor;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.List;

import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;
import static ptrman.math.ArrayRealVectorHelper.integerToArrayRealVector;

public class ProcessBTest {
    @Test
    public void test() {
        final BufferedImage testImage = drawTestImage();
        final IMap2d<ColorRgb> colorMap = translateFromImageToMap(testImage);

        VisualProcessor.ProcessingChain processingChain;

        // setup the processing chain

        processingChain = new VisualProcessor.ProcessingChain();

        Dag.Element newDagElement;

        newDagElement = new Dag.Element(
                new VisualProcessor.ProcessingChain.ChainElementColorFloat(
                        new VisualProcessor.ProcessingChain.ConvertColorRgbToGrayscaleFilter(new ColorRgb(1.0f, 1.0f, 1.0f)),
                        "convertRgbToGrayscale",
                        new Vector2d<>(colorMap.getWidth(), colorMap.getLength())
                )
        );
        newDagElement.childIndices.add(1);

        processingChain.filterChainDag.elements.add(newDagElement);


        newDagElement = new Dag.Element(
                new VisualProcessor.ProcessingChain.ChainElementFloatBoolean(
                        new VisualProcessor.ProcessingChain.DitheringFilter(),
                        "dither",
                        new Vector2d<>(colorMap.getWidth(), colorMap.getLength())
                )
        );

        processingChain.filterChainDag.elements.add(newDagElement);


        processingChain.filterChain(colorMap);

        IMap2d<Boolean> mapBoolean = ((VisualProcessor.ProcessingChain.ApplyChainElement)processingChain.filterChainDag.elements.get(1).content).result;

        ProcessA processA = new ProcessA();

        IMap2d<Integer> dummyObjectIdMap = new Map2d<>(mapBoolean.getWidth(), mapBoolean.getLength());
        for( int y = 0; y < dummyObjectIdMap.getLength(); y++ ) {
            for( int x = 0; x < dummyObjectIdMap.getWidth(); x++ ) {
                dummyObjectIdMap.setAt(x, y, 0);
            }
        }

        // NOTE< we need to sample twice beause the samples will be modified >
        processA.set(mapBoolean.copy(), dummyObjectIdMap);
        List<ProcessA.Sample> samplesTest = processA.sampleImage();
        processA.set(mapBoolean.copy(), dummyObjectIdMap);
        List<ProcessA.Sample> samplesReference = processA.sampleImage();


        // put it into processB

        ProcessB processB = new ProcessB();
        processB.process(samplesTest, mapBoolean);


        // generate (correct) reference data
        SlowCorrectAlgorithm slowCorrectAlgorithm = new SlowCorrectAlgorithm();
        slowCorrectAlgorithm.process(samplesReference, mapBoolean);

        // compare
        for( int i = 0; i < samplesReference.size(); i++ ) {
            ProcessA.Sample referenceSample = samplesReference.get(i);
            ProcessA.Sample testSample = samplesTest.get(i);

            final double error = 0.01f;

            Assert.Assert(referenceSample.altitude + error > testSample.altitude && referenceSample.altitude - error < testSample.altitude, "");

        }
    }

    // implementation of the slow correct old algorithm
    // used to compare the results
    private static class SlowCorrectAlgorithm {
        /**
         *
         * we use the whole image, in phaeaco he worked with the incomplete image witht the guiding of processA, this is not implemented that way
         */
        public void process(List<ProcessA.Sample> samples, IMap2d<Boolean> image) {
            Vector2d<Integer> foundPosition;

            final int MAXRADIUS = 100;

            for( ProcessA.Sample iterationSample : samples ) {
                Tuple2<Vector2d<Integer>, Double> nearestResult;

                nearestResult = findNearestPositionWhereMapIs(false, arrayRealVectorToInteger(iterationSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN), image, MAXRADIUS);
                if( nearestResult == null ) {
                    iterationSample.altitude = ((MAXRADIUS+1)*2)*((MAXRADIUS+1)*2);
                    continue;
                }
                // else here

                iterationSample.altitude = nearestResult.e1;
            }

        }

        // TODO< move into external function >
        /**
         *
         * \return null if no point could be found in the radius
         */
        private static Tuple2<Vector2d<Integer>, Double> findNearestPositionWhereMapIs(final boolean value, final Vector2d<Integer> position, final IMap2d<Boolean> image, final int radius) {
            Vector2d<Integer> outwardIteratorOffsetUnbound;
            Vector2d<Integer> borderMin;
            Vector2d<Integer> borderMax;
            Vector2d<Integer> one;
            Vector2d<Integer> positionAsInt;

            outwardIteratorOffsetUnbound = new Vector2d<>(0, 0);
            borderMin = new Vector2d<>(0, 0);
            borderMax = new Vector2d<>(image.getWidth(), image.getLength());

            positionAsInt = position;

            one = new Vector2d<>(1, 1);

            for(;;) {
                if (-outwardIteratorOffsetUnbound.x > radius) {
                    break;
                }

                Vector2d<Integer> bestPosition = null;
                double bestDistanceSquared = Double.MAX_VALUE;

                final Vector2d<Integer> iteratorOffsetBoundMin = Vector2d.IntegerHelper.max(borderMin, Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt));
                final Vector2d<Integer> iteratorOffsetBoundMax = Vector2d.IntegerHelper.min4(borderMax, Vector2d.IntegerHelper.add(Vector2d.IntegerHelper.add(Vector2d.IntegerHelper.getScaled(outwardIteratorOffsetUnbound, -1), one), positionAsInt), borderMax, borderMax);

                for( int y = iteratorOffsetBoundMin.y; y < iteratorOffsetBoundMax.y; y++ ) {
                    for( int x = iteratorOffsetBoundMin.x; x < iteratorOffsetBoundMax.x; x++ ) {
                        // just find at the border
                        if (y == (iteratorOffsetBoundMin.y) || y == iteratorOffsetBoundMax.y - 1 || x == (iteratorOffsetBoundMin.x) || x == iteratorOffsetBoundMax.x - 1) {
                            final boolean valueAtPoint = image.readAt(x, y);

                            if (valueAtPoint == value) {
                                final ArrayRealVector diff = integerToArrayRealVector(new Vector2d<>(x, y)).subtract(integerToArrayRealVector(position));
                                final double currentDistanceSquared = diff.dotProduct(diff);

                                if( currentDistanceSquared < bestDistanceSquared ) {
                                    bestDistanceSquared = currentDistanceSquared;
                                    bestPosition = new Vector2d<>(x, y);
                                }

                                //return new Tuple2(new Vector2d<>(x, y), (double)getLength(sub(new Vector2d<>((float) x, (float) y), Vector2d.ConverterHelper.convertIntVectorToFloat(position))));
                            }
                        }
                    }
                }

                if( bestPosition != null ) {
                    return new Tuple2(bestPosition, java.lang.Math.sqrt(bestDistanceSquared));
                }

                outwardIteratorOffsetUnbound.x--;
                outwardIteratorOffsetUnbound.y--;
            }

            return null;
        }
    }



    // TODO< move this into the functionality of the visual processor >
    private static IMap2d<ColorRgb> translateFromImageToMap(BufferedImage javaImage) {
        DataBuffer imageBuffer = javaImage.getData().getDataBuffer();

        int bufferI;
        IMap2d<ColorRgb> convertedToMap;

        convertedToMap = new Map2d<>(javaImage.getWidth(), javaImage.getHeight());

        for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
        {
            int pixelValue;

            pixelValue = javaImage.getRGB(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth());

            Color c = new Color(pixelValue);

            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();

            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), new ColorRgb((float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f));
        }

        return convertedToMap;
    }


    private static BufferedImage drawTestImage() {
        final int RETINA_WIDTH = 160;
        final int RETINA_HEIGHT = 160;

        BufferedImage resultImage = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = resultImage.createGraphics();


        g2.setColor(Color.BLACK);

        g2.drawRect(0, 0, resultImage.getWidth(), resultImage.getHeight());

        g2.setColor(Color.WHITE);

        g2.drawPolygon(new int[]{10, 50, 30}, new int[]{20, 30, 60}, 3);


        g2.setColor(Color.WHITE);

        g2.drawRect(50, 40, 5, 50);

        return resultImage;
    }

}
