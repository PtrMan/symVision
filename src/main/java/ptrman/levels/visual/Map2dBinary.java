package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;

public class Map2dBinary {
    public static IMap2d<Boolean> negate(IMap2d<Boolean> input) {
        IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for (int y = 0; y < input.getLength(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                result.setAt(x, y, !input.readAt(x, y));
            }
        }

        return result;
    }

    public static IMap2d<Boolean> corode(IMap2d<Boolean> input) {
        IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for( int y = 1; y < input.getLength() - 1; y++ ) {
            for( int x = 1; x < input.getWidth() - 1; x++ ) {
                // optimized

                if(
                        !input.readAt(x-1,y-1) ||
                                !input.readAt(x,y-1) ||
                                !input.readAt(x+1,y-1) ||

                                !input.readAt(x-1,y) ||
                                !input.readAt(x,y) ||
                                !input.readAt(x+1,y) ||

                                !input.readAt(x-1,y+1) ||
                                !input.readAt(x,y+1) ||
                                !input.readAt(x+1,y+1)
                        ) {
                    continue;
                }

                result.setAt(x, y, true);
            }
        }

        return result;
    }

    // edge thinning
    // http://fourier.eng.hmc.edu/e161/lectures/morphology/node2.html
    public static IMap2d<Boolean> edgeThinning(IMap2d<Boolean> input) {
        boolean[] neightbors = new boolean[8];

        IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for( int y = 1; y < input.getLength() - 1; y++ ) {
            for( int x = 1; x < input.getWidth() - 1; x++ ) {
                boolean deletePixel;

                neightbors[0] = input.readAt(x-1,y-1);
                neightbors[1] = input.readAt(x,y-1);
                neightbors[2] = input.readAt(x+1,y-1);

                neightbors[3] = input.readAt(x+1,y);
                neightbors[4] = input.readAt(x+1,y+1);

                neightbors[5] = input.readAt(x,y+1);
                neightbors[6] = input.readAt(x-1,y+1);

                neightbors[7] = input.readAt(x-1,y);

                // count zero crossing
                int zeroCrossing = 0;

                for( int i = 0; i < 8-1; i++ ) {
                    if( neightbors[i] && !neightbors[i+1] ) {
                        zeroCrossing++;
                    }
                }

                // count set pixels
                int setPixels = 0;

                for( int i = 0; i < 8; i++ ) {
                    if( neightbors[i] ) {
                        setPixels++;
                    }
                }

                if(
                        setPixels == 0 ||
                                setPixels == 1 ||
                                setPixels == 7 ||
                                setPixels == 8 ||
                                zeroCrossing >= 2
                        ) {
                    deletePixel = true;
                }
                else {
                    deletePixel = false;
                }

                if( !deletePixel ) {
                    result.setAt(x, y, true);
                }
            }
        }

        return result;
    }

    // http://fourier.eng.hmc.edu/e161/lectures/morphology/node3.html
    public static IMap2d<Boolean> skeletalize(IMap2d<Boolean> input) {
        int x, y;

        int[] minArray = new int[5];
        int[] maxArray = new int[5];

        IMap2d<Integer> counterMap = new Map2d<>(input.getWidth(), input.getLength());
        IMap2d<Boolean> output = new Map2d<>(input.getWidth(), input.getLength());

        for( y = 0; y < input.getLength(); y++ ) {
            for( x = 0; x < input.getWidth(); x++ ) {
                if( input.readAt(x, y) ) {
                    counterMap.setAt(x, y, 1);
                }
            }
        }

        int k = 0;

        for(;;) {
            k += 1;

            boolean changeMade = false;

            for( y = 1; y < input.getLength() - 1; y++ ) {
                for( x = 1; x < input.getWidth() - 1; x++ ) {
                    int min;
                    int newValue;

                    if( counterMap.readAt(x, y) != k ) {
                        continue;
                    }
                    // we are here if it is the case

                    changeMade = true;

                    //minArray[0] = counterMap.readAt(x-1, y-1);
                    minArray[0] = counterMap.readAt(x, y-1);
                    //minArray[2] = counterMap.readAt(x+1, y-1);

                    minArray[1] = counterMap.readAt(x-1, y);
                    minArray[2] = counterMap.readAt(x, y);
                    //minArray[2] = counterMap.readAt(x, y);
                    minArray[3] = counterMap.readAt(x+1, y);

                    //minArray[6] = counterMap.readAt(x-1, y+1);
                    minArray[4] = counterMap.readAt(x, y+1);
                    //minArray[8] = counterMap.readAt(x+1, y+1);

                    min = minOfArray(minArray);
                    newValue = min + 1;

                    counterMap.setAt(x, y, newValue);

                    //output.writeAt(x, y, true);
                }
            }

            if( !changeMade ) {
                break;
            }
        }

        for (y = 1; y < input.getLength()-1; y++) {
            for (x = 1; x < input.getWidth()-1; x++) {
                int compare;

                compare = counterMap.readAt(x, y);

                if( compare == 0 ) {
                    continue;
                }

                maxArray[0] = counterMap.readAt(x+1, y);
                maxArray[1] = counterMap.readAt(x-1, y);
                maxArray[2] = counterMap.readAt(x, y+1);
                maxArray[3] = counterMap.readAt(x, y-1);
                maxArray[4] = counterMap.readAt(x, y);

                if( compare == maxOfArray(maxArray) ) {
                    output.setAt(x, y, true);
                }
            }
        }

        return output;
    }

    public static IMap2d<Boolean> threshold(IMap2d<Float> input, float threshold) {
        IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for( int y = 0; y < input.getLength(); y++ ) {
            for( int x = 0; x < input.getWidth(); x++ ) {
                if( input.readAt(x, y) > threshold ) {
                    result.setAt(x, y, true);
                }
            }
        }

        return result;
    }

    public static void orInplace(final IMap2d<Boolean> a, final IMap2d<Boolean> b, IMap2d<Boolean> result) {
        for( int y = 0; y < a.getLength(); y++ ) {
            for( int x = 0; x < a.getWidth(); x++ ) {
                result.setAt(x, y, a.readAt(x, y) || b.readAt(x, y));
            }
        }
    }

    public static void andInplace(final IMap2d<Boolean> a, final IMap2d<Boolean> b, IMap2d<Boolean> result) {
        for( int y = 0; y < a.getLength(); y++ ) {
            for( int x = 0; x < a.getWidth(); x++ ) {
                result.setAt(x, y, a.readAt(x, y) && b.readAt(x, y));
            }
        }
    }

    public static void xorInplace(final IMap2d<Boolean> a, final IMap2d<Boolean> b, IMap2d<Boolean> result) {
        for( int y = 0; y < a.getLength(); y++ ) {
            for( int x = 0; x < a.getWidth(); x++ ) {
                result.setAt(x, y, a.readAt(x, y) ^ b.readAt(x, y));
            }
        }
    }



    private static int minOfArray(int[] array) {
        int min = array[0];

        for( int i = 1; i < array.length; i++ ) {
            if( array[i] < min ) {
                min = array[i];
            }
        }

        return min;
    }

    private static int maxOfArray(int[] array) {
        int max = array[0];

        for( int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }
}
