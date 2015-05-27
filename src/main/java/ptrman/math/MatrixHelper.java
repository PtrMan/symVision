package ptrman.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;

public class MatrixHelper {
    public static float[] convertMatrixToArray(final Array2DRowRealMatrix matrix) {
        float[] result = new float[16];

        for( int row = 0; row < 4; row++ ) {
            for( int column = 0; column < 4; column++ ) {
                result[row*4+column] = (float)matrix.getRow(row)[column];
            }
        }

        return result;
    }

    // see https://www.opengl.org/discussion_boards/showthread.php/172280-Constructing-an-orthographic-matrix-for-2D-drawing
    /* TODO< express it as a matrix >
    private static Array2DRowRealMatrix getOrtographicMatrix(float xmax, float ymax, float zNear, float zFar) {
        return new float[] {
                2.0f/xmax, 0.0f, 0.0f, -1.0f,
                0.0f -2.0f/ymax, 0.0f, 1.0f,
                0.0f, 0.0f, 2.0f/(zFar-zNear), (zNear+zFar)/(zNear-zFar),
                0.0f, 0.0f, 0.0f, 1.0f
        };
    }
    */

    public static Array2DRowRealMatrix getIdentityMatrix() {
        Array2DRowRealMatrix result = new Array2DRowRealMatrix(4, 4);
        for( int i = 0; i < 4; i++ ) {
            result.setEntry(i, i, 1);
        }

        return result;
    }

    public static Array2DRowRealMatrix getTranslationMatrix(final ArrayRealVector translation) {
        Array2DRowRealMatrix result = getIdentityMatrix();
        result.setEntry(3, 0, translation.getDataRef()[0]);
        result.setEntry(3, 1, translation.getDataRef()[1]);
        result.setEntry(3, 2, translation.getDataRef()[2]);
        return result;
    }

    public static Array2DRowRealMatrix getScaleMatrix(final ArrayRealVector scale) {
        Array2DRowRealMatrix result = getIdentityMatrix();
        result.setEntry(0, 0, scale.getDataRef()[0]);
        result.setEntry(1, 1, scale.getDataRef()[1]);
        result.setEntry(2, 2, scale.getDataRef()[2]);
        result.setEntry(3, 3, 1.0);
        return result;
    }
}
