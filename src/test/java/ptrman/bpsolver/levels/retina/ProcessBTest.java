package ptrman.bpsolver.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.Test;
import ptrman.Datastructures.*;
import ptrman.levels.retina.ProcessA;
import ptrman.levels.visual.ColorRgb;
import ptrman.levels.visual.VisualProcessor;
import ptrman.math.ArrayRealVectorHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.List;

import static ptrman.levels.retina.LineDetectorWithMultiplePoints.real;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;
import static ptrman.math.ArrayRealVectorHelper.integerToArrayRealVector;

public class ProcessBTest {
    @Test
    public void test() {
        final var testImage = drawTestImage();
        final var colorMap = translateFromImageToMap(testImage);

        // setup the processing chain

        final var processingChain = new VisualProcessor.ProcessingChain();

        var newDagElement = new Dag.Element(
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

        final IMap2d<Boolean> mapBoolean = ((VisualProcessor.ProcessingChain.ApplyChainElement)processingChain.filterChainDag.elements.get(1).content).result;

        final var processA = new ProcessA();

        final IMap2d<Integer> dummyObjectIdMap = new Map2d<>(mapBoolean.getWidth(), mapBoolean.getLength());
        for(var y = 0; y < dummyObjectIdMap.getLength(); y++ )
            for (var x = 0; x < dummyObjectIdMap.getWidth(); x++) dummyObjectIdMap.setAt(x, y, 0);

        /*

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

        */
    }

    // implementation of the slow correct old algorithm
    // used to compare the results
    private static class SlowCorrectAlgorithm {
        /**
         *
         * we use the whole image, in phaeaco he worked with the incomplete image witht the guiding of processA, this is not implemented that way
         */
        public void process(final List<ProcessA.Sample> samples, final IMap2d<Boolean> image) {
            Vector2d<Integer> foundPosition;

            final var MAXRADIUS = 100;

            for( final var iterationSample : samples ) {

                final var nearestResult = findNearestPositionWhereMapIs(false,
                    arrayRealVectorToInteger(real(iterationSample.position), ArrayRealVectorHelper.EnumRoundMode.DOWN), image, MAXRADIUS);

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

            final var outwardIteratorOffsetUnbound = new Vector2d<Integer>(0, 0);
            final var borderMin = new Vector2d<Integer>(0, 0);
            final var borderMax = new Vector2d<Integer>(image.getWidth(), image.getLength());

            final var positionAsInt = position;

            final var one = new Vector2d<Integer>(1, 1);

            while (-outwardIteratorOffsetUnbound.x <= radius) {

                Vector2d<Integer> bestPosition = null;
                var bestDistanceSquared = Double.MAX_VALUE;

                final var iteratorOffsetBoundMin = Vector2d.IntegerHelper.max(borderMin, Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt));
                final var iteratorOffsetBoundMax = Vector2d.IntegerHelper.min4(borderMax, Vector2d.IntegerHelper.add((Vector2d<Integer>) Vector2d.IntegerHelper.add(
                    Vector2d.IntegerHelper.getScaled(outwardIteratorOffsetUnbound, -1),
                    one), positionAsInt), borderMax, borderMax);

                // just find at the border
                for (int y = iteratorOffsetBoundMin.y; y < iteratorOffsetBoundMax.y; y++)
                    for (int x = iteratorOffsetBoundMin.x; x < iteratorOffsetBoundMax.x; x++)
                        if (y == (iteratorOffsetBoundMin.y) || y == iteratorOffsetBoundMax.y - 1 || x == (iteratorOffsetBoundMin.x) || x == iteratorOffsetBoundMax.x - 1) {
                            final boolean valueAtPoint = image.readAt(x, y);

                            if (valueAtPoint == value) {
                                final var diff = integerToArrayRealVector(new Vector2d<>(x, y)).subtract(integerToArrayRealVector(position));
                                final var currentDistanceSquared = diff.dotProduct(diff);

                                if (currentDistanceSquared < bestDistanceSquared) {
                                    bestDistanceSquared = currentDistanceSquared;
                                    bestPosition = new Vector2d<>(x, y);
                                }

                                //return new Tuple2(new Vector2d<>(x, y), (double)getLength(sub(new Vector2d<>((float) x, (float) y), Vector2d.ConverterHelper.convertIntVectorToFloat(position))));
                            }
                        }

                if (bestPosition != null) return new Tuple2(bestPosition, Math.sqrt(bestDistanceSquared));

                outwardIteratorOffsetUnbound.x--;
                outwardIteratorOffsetUnbound.y--;
            }

            return null;
        }
    }



    // TODO< move this into the functionality of the visual processor >
    private static IMap2d<ColorRgb> translateFromImageToMap(final BufferedImage javaImage) {
        final var imageBuffer = javaImage.getData().getDataBuffer();

        final IMap2d<ColorRgb> convertedToMap = new Map2d<>(javaImage.getWidth(), javaImage.getHeight());

        for(int bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
        {

            final var pixelValue = javaImage.getRGB(bufferI % convertedToMap.getWidth(), bufferI / convertedToMap.getWidth());


            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(),
                //new ColorRgb((float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f)
                new ColorRgb(pixelValue)
            );
        }

        return convertedToMap;
    }


    private static BufferedImage drawTestImage() {
        final var RETINA_WIDTH = 256;
        final var RETINA_HEIGHT = 256;

        final var resultImage = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        final var g2 = resultImage.createGraphics();


        g2.setColor(Color.BLACK);

        g2.drawRect(0, 0, resultImage.getWidth(), resultImage.getHeight());

        g2.setColor(Color.WHITE);

        g2.drawPolygon(new int[]{10, 50, 30}, new int[]{20, 30, 60}, 3);


        g2.setColor(Color.WHITE);

        g2.drawRect(50, 40, 5, 50);

        return resultImage;
    }

}
