package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.meter.event.DurationStartMeter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class VisualProcessor
{


    /**
     *
     * color to grayscale
     *
     * encapsulated for readable code
     *
     * TODO opencl
     */
    private static void convertColorToGrayImage(IMap2d<ColorRgb> inputColorImage, IMap2d<Float> outputGrayImage, ColorRgb colorScale)
    {
        class ConvertToGrayImageMap2dMapper implements Map2dMapper.IMap2dMapper<ColorRgb, Float>
        {
            private final ColorRgb colorScale;

            public ConvertToGrayImageMap2dMapper(ColorRgb colorScale)
            {
                this.colorScale = colorScale;
            }

            @Override
            public Float calculate(ColorRgb value)
            {
                return value.getScaledNormalizedMagnitude(colorScale);
            }
        }

        ConvertToGrayImageMap2dMapper imageMapper;
        Map2dMapper.Mapper<ColorRgb, Float> mapper;

        imageMapper = new ConvertToGrayImageMap2dMapper(colorScale);
        mapper = new Map2dMapper.Mapper<>();

        mapper.map(imageMapper, inputColorImage, outputGrayImage);
    }

    /**
     *
     * TODO opencl
     */
    private static void convertZeroCrossingFloatToBoolean(IMap2d<Float> input, IMap2d<Boolean> result)
    {
        class ConvertFloatZeroCrossingToBoolean implements Map2dMapper.IMap2dMapper<Float, Boolean>
        {
            @Override
            public Boolean calculate(Float value)
            {
                return value > 0.0f;
            }
        }

        ConvertFloatZeroCrossingToBoolean imageMapper;
        Map2dMapper.Mapper<Float, Boolean> mapper;

        imageMapper = new ConvertFloatZeroCrossingToBoolean();
        mapper = new Map2dMapper.Mapper<>();

        mapper.map(imageMapper, input, result);
    }

    public static class ProcessingChain
    {
        public static class MarrHildrethOperatorParameter
        {
            public int filterSize;
            public float sigma;
        }

        public ProcessingChain()
        {
            durationMeters.put("convertColorToGray", new DurationStartMeter("convertColorToGray", true, 1.0, false));
            durationMeters.put("zeroCrossingConvolution", new DurationStartMeter("zeroCrossingConvolution", true, 1.0, false));
            durationMeters.put("zeroCrossingToBoolean", new DurationStartMeter("zeroCrossingToBoolean", true, 1.0, false));

        }

        public void setup(Vector2d<Integer> imageSize, ColorRgb colorToGrayColorScale, MarrHildrethOperatorParameter grayImageZeroCrossingParameter)
        {
            grayImage = new Map2d<>(imageSize.x, imageSize.y);
            zeroCrossingBinary = new Map2d<>(imageSize.x, imageSize.y);

            grayImageZeroCrossingKernel = Convolution2dHelper.calculateMarrHildrethOperator(new Vector2d<>(grayImageZeroCrossingParameter.filterSize, grayImageZeroCrossingParameter.filterSize), grayImageZeroCrossingParameter.sigma);

            this.colorToGrayColorScale = colorToGrayColorScale;
        }

        public void filterChain(IMap2d<ColorRgb> inputColorImage)
        {
            /**
             * first we transform the inputimage to blackwhite
             * then we calculate the zero crossings of it.
             *
             * This seems to be a natural image processing principle because it works with grayscale image and color images in the same way.
             *
             */

            IMap2d<Float> zeroCrossings;

            durationMeters.get("convertColorToGray").start();
            convertColorToGrayImage(inputColorImage, grayImage, colorToGrayColorScale);
            durationMeters.get("convertColorToGray").stop();

            durationMeters.get("zeroCrossingConvolution").start();
            zeroCrossings = Convolution2d.convolution(grayImage, grayImageZeroCrossingKernel);
            durationMeters.get("zeroCrossingConvolution").stop();

            durationMeters.get("zeroCrossingToBoolean").start();
            convertZeroCrossingFloatToBoolean(zeroCrossings, zeroCrossingBinary);
            durationMeters.get("zeroCrossingToBoolean").stop();
        }

        public IMap2d<Boolean> getZeroCrossingBinary()
        {
            return zeroCrossingBinary;
        }

        private IMap2d<Float> grayImageZeroCrossingKernel;
        private IMap2d<Float> grayImage;

        private IMap2d<Boolean> zeroCrossingBinary;

        private ColorRgb colorToGrayColorScale;

        public Map<String, DurationStartMeter> durationMeters = new HashMap<>();
    }
}
