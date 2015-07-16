package ptrman.Datastructures;

import ptrman.levels.retina.helper.SpatialDrawer;
import ptrman.math.Maths;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceleration for quicker common operations on Map2d<Boolean>
 */
public class SpatialAcceleratedMap2d {


    private enum EnumGridCellState {
        FULLYSET,
        NOTFULLYSET,
        CLEAR
    }

    // size of map must be .x % gridsize = 0 and .y % gridsize = 0
    public SpatialAcceleratedMap2d(IMap2d<Boolean> map, final int gridsize) {
        Assert.Assert((map.getWidth() % gridsize) == 0, "width of map is not divisable by gridsize");
        Assert.Assert((map.getLength() % gridsize) == 0, "height of map is not divisable by gridsize");

        this.map = map;
        this.gridsize = gridsize;
        this.gridBoundary = new Vector2d<>(map.getWidth() / gridsize, map.getLength() / gridsize);

        gridCellStateMap = new Map2d<>(map.getWidth() / gridsize, map.getLength() / gridsize);
    }

    public Vector2d<Integer> getGridPositionOfPosition(final Vector2d<Integer> position) {
        return new Vector2d<>(position.x / gridsize, position.y / gridsize);
    }

    public List<Vector2d<Integer>> getGridLocationsOfGridRadius(final Vector2d<Integer> gridPosition, final int gridRadius) {
        return SpatialDrawer.getPositionsOfCellsOfCircleBound(gridPosition, gridRadius, gridBoundary);
    }

    public List<Vector2d<Integer>> getGridLocationsWithNegativeDirectionOfGridRadius(final Vector2d<Integer> gridPosition, final int gridRadius) {
        return SpatialDrawer.getPositionsOfCellsWithNegativeDirectionOfCircleBound(gridPosition, gridRadius, gridBoundary);
    }

    public List<List<Vector2d<Integer>>> getGridLocationsNearPositionInWideningRadius(final Vector2d<Integer> position, final float radius) {
        final Vector2d<Integer> centerGridLocation = getGridPositionOfPosition(position);

        List<List<Vector2d<Integer>>> result = new ArrayList<>();

        final int radiusInBlocks = 2 + (int)radius / gridsize;

        for( int radiusI = 0; radiusI < radiusInBlocks; radiusI++ ) {
            result.add(getGridLocationsOfGridRadius(centerGridLocation, radiusI));
        }

        return result;
    }

    public void recalculateGridCellStateMap() {
        final int numberOfPixelsPerCell = gridsize*gridsize;

        for( int y = 0; y < gridBoundary.y; y++ ) {
            for( int x = 0; x < gridBoundary.x; x++ ) {
                int numberOfPixelsSetInCell = countPixelsOfGridCell(x, y);

                if( numberOfPixelsPerCell == numberOfPixelsSetInCell ) {
                    gridCellStateMap.setAt(x, y, EnumGridCellState.FULLYSET);
                }
                else if( numberOfPixelsPerCell == 0 ) {
                    gridCellStateMap.setAt(x, y, EnumGridCellState.CLEAR);
                }
                else {
                    gridCellStateMap.setAt(x, y, EnumGridCellState.NOTFULLYSET);
                }
            }
        }
    }

    public boolean canValueBeFoundInCell(final Vector2d<Integer> cellPosition, final boolean value) {
        final EnumGridCellState gridCellStateAtPosition = gridCellStateMap.readAt(cellPosition.x, cellPosition.y);

        if( value ) {
            return gridCellStateAtPosition != EnumGridCellState.CLEAR;
        }
        else {
            return gridCellStateAtPosition != EnumGridCellState.FULLYSET;
        }
    }

    public int getGridsize() {
        return gridsize;
    }

    public Vector2d<Integer> getSize() {
        return new Vector2d<>(gridCellStateMap.getWidth(), gridCellStateMap.getLength());
    }

    private int countPixelsOfGridCell(final int cellX, final int cellY) {
        int numberOfPixels = 0;

        for( int y = cellY*gridsize; y < (cellY+1)*gridsize; y++ ) {
            for( int x = cellX*gridsize; x < (cellX+1)*gridsize; x++) {
                if( map.readAt(x, y) ) {
                    numberOfPixels++;
                }
            }
        }

        return numberOfPixels;
    }

    // TODO< move outside >
    private static Vector2d<Integer> clampVector2dInteger(final Vector2d<Integer> position, final Vector2d<Integer> min, final Vector2d<Integer> max) {
        final int x = Maths.clampInt(position.x, min.x, max.x);
        final int y = Maths.clampInt(position.y, min.y, max.y);
        return new Vector2d<>(x, y);
    }

    private IMap2d<Boolean> map;
    private final int gridsize;
    private final Vector2d<Integer> gridBoundary;

    public IMap2d<EnumGridCellState> gridCellStateMap;
}
