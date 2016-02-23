package ptrman.cvHelper;

/**
 * Created by r0b3 on 17.08.15.
 */
public class Morphology {
    /**
     * Kernel for small 'kernel' maps of a TODO operation.
     * The usage code doesn't optimize the kernel in any way so the kernel should be relativly small.
     *
     */
    // TODO< correct name after literature >
    private static final String program =
            "#define ARRAY2d(array, width, x, y) (array[(x) + (y)*(width)])" + "\n" +
            "" + "\n" +
            "kernel void multiplySmallUnoptimized(" + "\n" +
            "__global int* inputMap," + "\n" +
            "__global int* resultMap," + "\n" +
            "int mapSizeX," + "\n" +
            "int mapSizeY," + "\n" +
            "" + "\n" +
            "__global int* commandPositions," + "\n" +
            "int commandSize" + "\n" +
            ") {" + "\n" +
            "int indexX = get_global_id(0);" + "\n" +
            "int indexY = get_global_id(1);" + "\n" +
            "" + "\n" +
            "if( indexX >= mapSizeX ) {" + "\n" +
            "   return;" + "\n" +
            "}" + "\n" +
            "if( indexY >= mapSizeY ) {" + "\n" +
            "   return;" + "\n" +
            "}" + "\n" +
            "" + "\n" +
            "int outputPixel = 1;" + "\n" +
            "" + "\n" +
            "for( int commandI = 0; commandI < commandSize; commandI++) {" + "\n" +
            "   int offsetX = commandPositions[commandI*2 + 0];" + "\n" +
            "   int offsetY = commandPositions[commandI*2 + 1];" + "\n" +
            "   " + "\n" +
            "   int readIndexX = indexX + offsetX;" + "\n" +
            "   int readIndexY = indexY + offsetY;" + "\n" +
            "   " + "\n" +
            "   if( readIndexX < 0 || readIndexX >= mapSizeX || readIndexY < 0 || readIndexY >= mapSizeY ) {" + "\n" +
            "       outputPixel = 0;" + "\n" +
            "       break;" + "\n" +
            "   }" + "\n" +
            "   " + "\n" +
            "   int readPixel = ARRAY2d(inputMap, mapSizeX, readIndexX, readIndexY);" + "\n" +
            "   if( !readPixel ) {" + "\n" +
            "       outputPixel = 0;" + "\n" +
            "       break;" + "\n" +
            "   }" + "\n" +
            "}" + "\n" +
            "" + "\n" +
            "ARRAY2d(resultMap, mapSizeX, indexX, indexY) = outputPixel;" + "\n" +
            "" + "\n" +
            "" + "\n" +
            "}";
}
