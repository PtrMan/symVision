package cg4j;

import cg4j.exception.IllegalShapeException;
import cg4j.node.io.VariableNode;

import java.util.Arrays;
import java.util.Random;

/**
 * The Tensor class represents an N-Dimensional Array, also called a tensor.
 *
 * @author nathanwood1
 * @since 1.0
 */
public class Tensor implements java.io.Serializable {
	/**
	 * Length of the data.
	 * Equivalent to {@code vals.length}.
	 *
	 * @since 1.0
	 */
	public final int length;
	public final int[] shape;

	private final float[] vals;

	/**
	 * Creates a tensor from an array of values and a shape.
	 *
	 * @param vals  A {@code float[]} of values. The dimensionality is added by {@code shape}.
	 * @param shape An {@code int[]} of dimensions. This gives {@code vals} dimension.
	 * @throws IllegalShapeException if the given shape contains any values <= 0.
	 * @throws IllegalShapeException if the length of {@code vals} doesn't equal the length given by {@code shape}
	 * @since 1.0
	 */
	public Tensor(float[] vals, int... shape) {

		int length = 1; //Get the length by multiplying all the values in 'shape' together
		for (int x : shape) {
			if (x <= 0)
				throw new IllegalShapeException("Shape component must be positive", shape);
			length *= x;
		}
		if (vals.length != length) //If the shape's values don't match the vals' value, throw an exception
			throw new IllegalShapeException("Shape doesn't match length=" + length, shape);

		this.vals = vals; //Assign the inputs
		this.shape = shape;
		this.length = length;
	}

	private Tensor(float[] vals, int[] shape, int length) {
		this.vals = vals;
		this.shape = shape;
		this.length = length;
	}

	/**
	 * Create a Tensor from {@code Math.random()}
	 *
	 * @param lb    Lower bound of randomness.
	 * @param ub    Upper bound of randomness.
	 * @param shape The shape of the tensor.
	 * @return The tensor.
	 * @since 1.0
	 */
	public static Tensor fromRandom(float lb, float ub, int[] shape) {
		int length = 1; // Get the length by multiplying the shapes together
		for (int x : shape) {
			length *= x;
		}

		float[] vals = new float[length];
		for (int i = 0; i < length; i++) {
			vals[i] = ((float) Math.random()) * (ub - lb) + lb; // Put the random value between lb and ub
		}

		return new Tensor(vals, shape, length);
	}

	public static VariableNode variableRandom(Random random, float lb, float ub, int[] shape) {
		return new VariableNode(fromRandom(random, lb, ub, shape));
	}

	/**
	 * Create a Tensor from {@code Random}
	 *
	 * @param lb    Lower bound of randomness.
	 * @param ub    Upper bound of randomness.
	 * @param shape The shape of the tensor.
	 * @return The tensor.
	 * @see java.util.Random
	 * @since 1.0
	 */
	public static Tensor fromRandom(Random random, float lb, float ub, int[] shape) {
		int length = 1; // Get the length by multiplying the shapes together
		for (int x : shape) {
			length *= x;
		}

		float[] vals = new float[length];
		for (int i = 0; i < length; i++) {
			vals[i] = random.nextFloat() * (ub - lb) + lb; // Put the random value between lb and ub
		}

		return new Tensor(vals, shape, length);
	}

	/**
	 * Returns the value at {@code i} based on {@code vals}
	 *
	 * @param i The index to get.
	 * @return The value at {@code i}.
	 * @since 1.0
	 */
	public float get(int i) {
		return vals[i];
	}

	/**
	 * Set the value at {@code i} to {@code val}.
	 *
	 * @param i   The index to set.
	 * @param val The value to set.
	 * @since 1.0
	 */
	public void set(int i, float val) {
		vals[i] = val;
	}

	/**
	 * Get the value at a list of indices. This has dimensionality.
	 *
	 * @param indices The list of indices to get.
	 * @return The value at {@code indices}.
	 * @since 1.0
	 */
	public float get(int[] indices) {
		int i = 0;
		for (int j = 0; j < indices.length; j++) { //Convert the indices into a single index
			int iI = indices[j];
			for (int k = indices.length - 1; k > j; k--) {
				iI *= shape[k];
			}
			i += iI;
		}
		return vals[i];
	}

	/**
	 * Set the value at a list of indices. This has dimensionality.
	 *
	 * @param indices The list of indices to set.
	 * @param val     The value to set.
	 * @since 1.0
	 */
	public void set(int[] indices, float val) {
		int i = 0;
		for (int j = 0; j < indices.length; j++) { //Convert the indices into a single index
			int iI = indices[j];
			for (int k = indices.length - 1; k > j; k--) {
				iI *= shape[k];
			}
			i += iI;
		}
		vals[i] = val;
	}

	/**
	 * Converts a list of indices into a single index.
	 *
	 * @param indices The list of indices.
	 * @return The index.
	 * @since 1.0
	 */
	public int getIndexFromIndices(int[] indices) {
		int i = 0;
		for (int j = 0; j < indices.length; j++) { //Convert the indices into a single index
			int iI = indices[j];
			for (int k = indices.length - 1; k > j; k--) {
				iI *= shape[k];
			}
			i += iI;
		}
		return i;
	}

	/**
	 * Converts an index into a list of indices.
	 *
	 * @param i The index.
	 * @return The list of indices.
	 * @since 1.0
	 */
	public int[] getIndicesFromIndex(int i) {
		int[] indices = new int[shape.length];
		for (int j = 0; j < shape.length; j++) { // Convert the index into an array of indices
			int iI = 1;
			for (int k = shape.length - 1; k > j; k--) {
				iI *= shape[k];
			}
			indices[j] = i / iI;
			i -= (i / iI) * iI;
		}
		return indices;
	}

	/**
	 * Checks if an object is equal to this tensor.
	 *
	 * @param o The object to check.
	 * @return Whether the object equals this tensor.
	 * @since 1.0
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Tensor) { // Check 'o' is a Tensor
			Tensor other = (Tensor) o;
			if (Arrays.equals(other.shape, this.shape)) {
				return Arrays.equals(other.vals, this.vals);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		//TODO refine
		int result = length;
		result = 31 * result + Arrays.hashCode(shape);
		result = 31 * result + Arrays.hashCode(vals);
		return result;
	}

	/**
	 * Returns a string summary of the tensor.
	 *
	 * @return String summary.
	 * @since 1.0
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tensor(Shape="); // Display data about the object
		sb.append(Arrays.toString(shape));
		sb.append(", Data=");

		int[] shapeI = new int[shape.length];
		sb.append(new String(new byte[shape.length]).replaceAll("\u0000", "[")); // Open initial brackets
		for (int j = 0; j < length; j++) {
			sb.append(vals[j]); // Add the value
			int closed = 0; // Check and count how many brackets were closed
			for (int k = shape.length - 1; k >= 0; k--) {
				shapeI[k]++;
				if (shapeI[k] == shape[k]) {
					shapeI[k] = 0;
					closed++;
				} else {
					break;
				}
			}
			if (closed == 0) { // If none, add a comma
				sb.append(", ");
			} else if (j == length - 1) { // If we're at the end, close all brackets
				sb.append(new String(new byte[closed]).replaceAll("\u0000", "]"));
			} else { // Otherwise, close brackets and open them again
				sb.append(new String(new byte[closed]).replaceAll("\u0000", "]"));
				sb.append(", ");
				sb.append(new String(new byte[closed]).replaceAll("\u0000", "["));
			}
		}
		sb.append(")");

		return sb.toString();
	}

	/**
	 * Same as toString() but doesn't display 'Tensor(Shape=...'
	 *
	 * @return String summary.
	 * @since 1.0
	 */
	public String arrayToString() {
		StringBuilder sb = new StringBuilder();

		int[] shapeI = new int[shape.length];
		sb.append(new String(new byte[shape.length]).replaceAll("\u0000", "[")); // Open initial brackets
		for (int j = 0; j < length; j++) {
			sb.append(vals[j]); // Add the value
			int closed = 0; // Check and count how many brackets were closed
			for (int k = shape.length - 1; k >= 0; k--) {
				shapeI[k]++;
				if (shapeI[k] == shape[k]) {
					shapeI[k] = 0;
					closed++;
				} else {
					break;
				}
			}
			if (closed == 0) { // If none, add a comma
				sb.append(", ");
			} else if (j == length - 1) { // If we're at the end, close all brackets
				sb.append(new String(new byte[closed]).replaceAll("\u0000", "]"));
			} else { // Otherwise, close brackets and open them again
				sb.append(new String(new byte[closed]).replaceAll("\u0000", "]"));
				sb.append(", ");
				sb.append(new String(new byte[closed]).replaceAll("\u0000", "["));
			}
		}

		return sb.toString();
	}

	/**
	 * Copies a portion from the values.
	 *
	 * @param toCopyTo The float[] to copy to.
	 * @param start    The start index.
	 * @param length   The length to copy.
	 */
	public float[] copy(float[] toCopyTo, int start, int length) {
		System.arraycopy(vals, start, toCopyTo, 0, length);
		return toCopyTo;
	}

	/**
	 * Returns 'vals'.
	 * This method is not recommended.
	 * Please use {@code Tensor#getVal} and {@code Tensor#setVal} instead.
	 *
	 * @return float[] of values.
	 * @see Tensor#get(int)
	 * @see Tensor#set(int, float)
	 */
	public float[] getVals() {
		return vals;
	}

}
