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

    public static Array2DRowRealMatrix getIdentityMatrix() {
        Array2DRowRealMatrix result = new Array2DRowRealMatrix(4, 4);
        for( int i = 0; i < 4; i++ ) {
            result.getData()[i][i] = 1.0;
        }

        return result;
    }

    public static Array2DRowRealMatrix getTranslationMatrix(final ArrayRealVector translation) {
        Array2DRowRealMatrix result = getIdentityMatrix();
        result.getData()[3][0] = translation.getDataRef()[0];
        result.getData()[3][1] = translation.getDataRef()[1];
        result.getData()[3][2] = translation.getDataRef()[2];
        return result;
    }
}
