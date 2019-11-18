/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;

public enum MatrixHelper {
	;

	public static float[] convertMatrixToArray(final RealMatrix matrix) {
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
