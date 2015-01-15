package RetinaLevel;

import Datastructures.Vector2d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;

/** curve detection
 *
 * 
 */
public class ProcessG
{
    // test, works
    public static void testPoints()
    {
        ArrayList<Vector2d<Float>> testPoints;
        
        testPoints = new ArrayList<>();
        testPoints.add(new Vector2d<>(1.0f, 5.0f));
        testPoints.add(new Vector2d<>(1.8f, 4.0f));
        testPoints.add(new Vector2d<>(2.0f, 7.0f));
        
        ArrayList<Curve> resultCurves;
        
        // works fine
        resultCurves = calculatePolynominalsAndReturnCurves(testPoints);
        
        int t = 0;
    }
    
    public static class Curve
    {
        public Curve(float a0, float a1, float a2, float a3, float b0, float b1, float b2, float b3)
        {
            a = new float[]{a0, a1, a2, a3};
            b = new float[]{b0, b1, b2, b3};
        }
        
        // parametric curve parameters
        private float[] a;
        private float[] b;
    }
    
    private static ArrayList<Curve> calculatePolynominalsAndReturnCurves(ArrayList<Vector2d<Float>> points)
    {
        RealVector solvedA_2_i;
        RealVector solvedA_1_i;
        RealVector solvedA_3_i;
        RealVector solvedA_0_i;
        
        RealVector solvedB_2_i;
        RealVector solvedB_1_i;
        RealVector solvedB_3_i;
        RealVector solvedB_0_i;
        
        solvedA_2_i = ProcessG.solveLinearEquationFor2ForPoints(points, EnumAxis.X);
        solvedA_1_i = ProcessG.calculate_1_i(points, solvedA_2_i, EnumAxis.X);
        solvedA_3_i = ProcessG.calculate_2_i(points, solvedA_2_i);
        solvedA_0_i = ProcessG.calculate_0_i(points, EnumAxis.X);
        
        solvedB_2_i = ProcessG.solveLinearEquationFor2ForPoints(points, EnumAxis.Y);
        solvedB_1_i = ProcessG.calculate_1_i(points, solvedB_2_i, EnumAxis.Y);
        solvedB_3_i = ProcessG.calculate_2_i(points, solvedB_2_i);
        solvedB_0_i = ProcessG.calculate_0_i(points, EnumAxis.Y);
        
        return createCurves(solvedA_0_i, solvedA_1_i, solvedA_2_i, solvedA_3_i, solvedB_0_i, solvedB_1_i, solvedB_2_i, solvedB_3_i);
    }
    
    // builds a linear equation for the a|b_2,i values and returns the coefficients
    // NOTE< points must be for sure sorted by x axis? >
    private static RealVector solveLinearEquationFor2ForPoints(ArrayList<Vector2d<Float>> points, EnumAxis axis)
    {
        Array2DRowRealMatrix matrix;
        RealVector constants;
        int i;
        
        // math libary usage see http://commons.apache.org/proper/commons-math/userguide/linear.html
        
        matrix = new Array2DRowRealMatrix(points.size(), points.size());
        
        // populate matrix
        
        //  top and bottom
        matrix.setEntry(0, 0, 1.0);
        matrix.setEntry(points.size()-1, points.size()-1, 1.0);
        
        // middle
        for( i = 1; i < points.size()-1; i++ )
        {
            matrix.setEntry(i, i-1, 1.0);
            matrix.setEntry(i, i-1+1, 4.0);
            matrix.setEntry(i, i-1+2, 1.0);
        }
        
        // populate constants
        
        constants = new ArrayRealVector(points.size());
        constants.setEntry(0, 0.0);
        constants.setEntry(points.size()-1, 0.0);
        
        for( i = 0; i < points.size()-2; i++ )
        {
            double value;
            
            value = 3.0*getAxisValueForPointOfArray(points, i, axis) - 6.0*getAxisValueForPointOfArray(points, i+1, axis) + 3.0*getAxisValueForPointOfArray(points, i+2, axis);
            constants.setEntry(i+1, value);
        }
        
        
        // solve system
        DecompositionSolver solver = new LUDecomposition(matrix).getSolver();
        RealVector solution = solver.solve(constants);
        
        return solution;
    }
    
    // calculates the (A|B)_1_i after Formula (9a) (foundalis dissertation page 422)
    // note that the result vector is one shorter than the input vector
    private static RealVector calculate_1_i(ArrayList<Vector2d<Float>> points, RealVector a_2_i, EnumAxis axis)
    {
        RealVector result;
        
        result = new ArrayRealVector(a_2_i.getDimension()-1);
        for( int i = 0; i < a_2_i.getDimension()-1; i++ )
        {
            double result_1_i;
            
            result_1_i = getAxisValueForPointOfArray(points, i+1, axis) - getAxisValueForPointOfArray(points, i, axis) - (1.0/3.0)*(2.0*a_2_i.getEntry(i) + a_2_i.getEntry(i+1));
            result.setEntry(i, result_1_i);
        }
        
        return result;
    }
    
    // calculate the (A|B)_3_i after formula (7)
    private static RealVector calculate_2_i(ArrayList<Vector2d<Float>> points, RealVector solved_2_i)
    {
        RealVector result;
        
        result = new ArrayRealVector(solved_2_i.getDimension()-1);
        for( int i = 0; i < solved_2_i.getDimension()-1; i++ )
        {
            double result_3_i = (solved_2_i.getEntry(i+1)-solved_2_i.getEntry(i))*0.3333333333333333333333333;
            result.setEntry(i, result_3_i);
        }
        
        return result;
    }
    
    // "calculate" the (A|B)_0_i after formula (4)
    private static RealVector calculate_0_i(ArrayList<Vector2d<Float>> points, EnumAxis axis)
    {
        RealVector result;
        
        result = new ArrayRealVector(points.size());
        
        for( int i = 0; i < points.size(); i++ )
        {
            double result_0_i;
            result_0_i = getAxisValueForPointOfArray(points, i, axis);
            result.setEntry(i, result_0_i);
        }
        
        return result;
    }
    
    
    private static ArrayList<Curve> createCurves(RealVector solvedA_0_i, RealVector solvedA_1_i, RealVector solvedA_2_i, RealVector solvedA_3_i, RealVector solvedB_0_i, RealVector solvedB_1_i, RealVector solvedB_2_i, RealVector solvedB_3_i) {
        ArrayList<Curve> resultCurves;
        int numberOfPoints;
        int curveI;
        
        numberOfPoints = solvedA_0_i.getDimension();
        
        resultCurves = new ArrayList<>();
        
        for( curveI = 0; curveI < numberOfPoints-1; curveI++ )
        {
            resultCurves.add(
                new Curve(
                    (float)solvedA_0_i.getEntry(curveI),
                    (float)solvedA_1_i.getEntry(curveI),
                    (float)solvedA_2_i.getEntry(curveI),
                    (float)solvedA_3_i.getEntry(curveI),
                    
                    (float)solvedB_0_i.getEntry(curveI),
                    (float)solvedB_1_i.getEntry(curveI),
                    (float)solvedB_2_i.getEntry(curveI),
                    (float)solvedB_3_i.getEntry(curveI)
                )
            );
        }
        
        return resultCurves;
    }
    
    
    private static float getAxisValueForPointOfArray(ArrayList<Vector2d<Float>> points, int index, EnumAxis axis)
    {
        return getAxisValueForPoint(points.get(index), axis);
    }
    
    private static float getAxisValueForPoint(Vector2d<Float> point, EnumAxis axis)
    {
        if( axis == EnumAxis.X )
        {
            return point.x;
        }
        else
        {
            return point.y;
        }
    }

    

    
    
    private enum EnumAxis
    {
        X,
        Y
    }
}
