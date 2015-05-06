package ptrman.Datastructures;

import ptrman.Algorithms.Bresenham;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceleration for quicker common operations on Map2d<Boolean>
 */
public class SpatialAcceleratedMap2d {
    static final private class Drawer implements Bresenham.IDrawer {
        public List<Vector2d<Integer>> positions = new ArrayList<>();

        @Override
        public void set(Vector2d<Integer> position) {
            positions.add(position);
        }
    }

    private enum EnumGridCellState {
        FULLYSET,
        NOTFULLYSET,
        CLEAR
    }

    // size of map must be .x % gridsize = 0 and .y % gridsize = 0
    public SpatialAcceleratedMap2d(IMap2d<Boolean> map, final int gridsize) {
        this.map = map;
        this.gridsize = gridsize;
        this.gridBoundary = new Vector2d<>(map.getWidth() / gridsize, map.getLength() / gridsize);

        gridCellStateMap = new Map2d<>(map.getWidth() / gridsize, map.getLength() / gridsize);
    }

    public List<List<Vector2d<Integer>>> getGridLocationsNearPositionInWideningRadius(final Vector2d<Integer> position, final float radius) {
        final Vector2d<Integer> centerGridLocation = new Vector2d<>(position.x / gridsize, position.y / gridsize);

        List<List<Vector2d<Integer>>> result = new ArrayList<>();

        final int radiusInBlocks = 1 + (int)radius / gridsize;

        for( int radiusI = 0; radiusI < radiusInBlocks; radiusI++ ) {
            Drawer drawer = new Drawer();

            Bresenham.rasterCircle(centerGridLocation, radiusI, drawer);

            drawer.positions.removeIf(position2 -> !isGridLocationInBound(position2));
            result.add(drawer.positions);
        }

        return result;
    }

    public void recalculateGridCellStateMap() {
        final int numberOfPixelsPerCell = gridsize*gridsize;

        for( int y = 0; y < gridBoundary.y; y++ ) {
            for( int x = 0; x < gridBoundary.x; x++ ) {
                int numberOfPixelsSetInCell = countPixelsOfGridCell(x, y);

                if( numberOfPixelsPerCell == numberOfPixelsPerCell ) {
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

    private boolean isGridLocationInBound(final Vector2d<Integer> gridPosition) {
        return gridPosition.x >= 0 && gridPosition.x < gridBoundary.x && gridPosition.y >= 0 && gridPosition.y < gridBoundary.y;
    }

    // TODO< move outside >
    private static Vector2d<Integer> clampVector2dInteger(final Vector2d<Integer> position, final Vector2d<Integer> min, final Vector2d<Integer> max) {
        final int x = ptrman.math.Math.clampInt(position.x, min.x, max.x);
        final int y = ptrman.math.Math.clampInt(position.y, min.y, max.y);
        return new Vector2d<>(x, y);
    }

    private IMap2d<Boolean> map;
    private final int gridsize;
    private final Vector2d<Integer> gridBoundary;

    public IMap2d<EnumGridCellState> gridCellStateMap;
}
