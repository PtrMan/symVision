/**
 * Copyright 2019 The SymVision authors
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Datastructures;

import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import ptrman.levels.retina.helper.SpatialDrawer;
import ptrman.math.Maths;

import java.util.Collection;
import java.util.List;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 * Acceleration for quicker common operations on Map2d<Boolean>
 */
public class SpatialAcceleratedMap2d {


	public final IMap2d<EnumGridCellState> gridCellStateMap;
	private final IMap2d<Boolean> map;
	private final int gridsize;
	private final IntIntPair gridBoundary;

	// size of map must be .x % gridsize = 0 and .y % gridsize = 0
	public SpatialAcceleratedMap2d(final IMap2d<Boolean> map, final int gridsize) {
		assert (map.getWidth() % gridsize) == 0 : "ASSERT: " + "width of map is not divisable by gridsize";
		assert (map.getLength() % gridsize) == 0 : "ASSERT: " + "height of map is not divisable by gridsize";

		this.map = map;
		this.gridsize = gridsize;
		this.gridBoundary = pair(map.getWidth() / gridsize, map.getLength() / gridsize);

		gridCellStateMap = new Map2d<>(map.getWidth() / gridsize, map.getLength() / gridsize);
	}

//    public List<List<IntIntPair>> getGridLocationsNearPositionInWideningRadius(final IntIntPair position, final float radius) {
//        final IntIntPair centerGridLocation = getGridPositionOfPosition(position);
//
//        List<List<IntIntPair>> result = new ArrayList<>();
//
//        final int radiusInBlocks = 2 + (int)radius / gridsize;
//
//        for( int radiusI = 0; radiusI < radiusInBlocks; radiusI++ )
//            result.add(getGridLocationsOfGridRadius(centerGridLocation, radiusI));
//
//        return result;
//    }

	// TODO< move outside >
	private static Vector2d<Integer> clampVector2dInteger(final Vector2d<Integer> position, final Vector2d<Integer> min, final Vector2d<Integer> max) {
		final var x = Maths.clampInt(position.x, min.x, max.x);
		final var y = Maths.clampInt(position.y, min.y, max.y);
		return new Vector2d<>(x, y);
	}

	public IntIntPair getGridPositionOfPosition(final IntIntPair position) {
		return pair(position.getOne() / gridsize, position.getTwo() / gridsize);
	}

	public List<IntIntPair> getGridLocationsOfGridRadius(final IntIntPair gridPosition, final int gridRadius) {
		return SpatialDrawer.getPositionsOfCellsOfCircleBound(gridPosition, gridRadius, gridBoundary);
	}

	public void getGridLocationsWithNegativeDirectionOfGridRadius(final IntIntPair gridPosition, final int gridRadius, final Collection<IntIntPair> resultPositions) {
		SpatialDrawer.getPositionsOfCellsWithNegativeDirectionOfCircleBound(gridPosition, gridRadius, gridBoundary, resultPositions);
	}

	public void recalculateGridCellStateMap() {
		final var numberOfPixelsPerCell = gridsize * gridsize;

		for (var y = 0; y < gridBoundary.getTwo(); y++)
			for (var x = 0; x < gridBoundary.getOne(); x++) {
				final var numberOfPixelsSetInCell = countPixelsOfGridCell(x, y);
				if (numberOfPixelsSetInCell == 0) gridCellStateMap.setAt(x, y, EnumGridCellState.CLEAR);
				else if (numberOfPixelsSetInCell == numberOfPixelsPerCell)
					gridCellStateMap.setAt(x, y, EnumGridCellState.FULLYSET);
				else gridCellStateMap.setAt(x, y, EnumGridCellState.NOTFULLYSET);
			}
	}

	public boolean canValueBeFoundInCell(final IntIntPair cellPosition, final boolean value) {
		final var gridCellStateAtPosition = gridCellStateMap.readAt(cellPosition.getOne(), cellPosition.getTwo());

		return value ? (gridCellStateAtPosition != EnumGridCellState.CLEAR) : (gridCellStateAtPosition != EnumGridCellState.FULLYSET);
	}

	public int getGridsize() {
		return gridsize;
	}

	public Vector2d<Integer> getSize() {
		return new Vector2d<>(gridCellStateMap.getWidth(), gridCellStateMap.getLength());
	}

	private int countPixelsOfGridCell(final int cellX, final int cellY) {
		var numberOfPixels = 0;

		for (var y = cellY * gridsize; y < (cellY + 1) * gridsize; y++)
			for (var x = cellX * gridsize; x < (cellX + 1) * gridsize; x++) if (map.readAt(x, y)) numberOfPixels++;

		return numberOfPixels;
	}

	private enum EnumGridCellState {
		FULLYSET,
		NOTFULLYSET,
		CLEAR
	}
}
