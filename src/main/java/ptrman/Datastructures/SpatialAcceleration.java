/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Datastructures;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * 
 */
public class SpatialAcceleration<Type> {
    public SpatialAcceleration(final int gridcountX, final int gridcountY, final float sizeX, final float sizeY) {
        this.gridcountX = gridcountX;
        this.gridcountY = gridcountY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.gridelementSizeX = sizeX/gridcountX;
        this.gridelementSizeY = sizeY/gridcountY;
        
        
        this.cells = createCells(gridcountX, gridcountY);
    }
    
    public void flushCells() {
        for( final var iterationCell : cells ) iterationCell.content.clear();
    }
    
    public void addElement(final Element element) {

        final var center = calcCenterInt(element.position);
        getCellAt(center.x, center.y).content.add(element);
    }
    
    public List<Element> getElementsNearPoint(final ArrayRealVector point, final float maximalRadius) {

        final var neightborRadiusInBlocks = calcNeightborRadiusInBlocks(maximalRadius);
        final var center = calcCenterInt(point);

        final var adjacentElements = getElementsInAdjacentCells(center, neightborRadiusInBlocks);
        final var adjacentElementsInRadius = getElementsInRadius(adjacentElements, point, maximalRadius);
        return adjacentElementsInRadius;
    }
    
    private Vector2d<Integer> calcNeightborRadiusInBlocks(final float maximalRadius) {
        Vector2d<Integer> neightborRadiusInBlocks;

        final var neightborRadiusInBlocksX = 1 + (int) (maximalRadius / gridelementSizeX);
        final var neightborRadiusInBlocksY = 1 + (int) (maximalRadius / gridelementSizeY);
        
        return new Vector2d<>(neightborRadiusInBlocksX, neightborRadiusInBlocksY);
    }
    
    private Vector2d<Integer> calcCenterInt(final ArrayRealVector point) {

        final var centerX = (int) (point.getDataRef()[0] / gridelementSizeX);
        final var centerY = (int) (point.getDataRef()[1] / gridelementSizeY);

        return new Vector2d<>(centerX, centerY);
    }
    
    private List<Element> getElementsInRadius(final Iterable<Element> adjacentElements, final ArrayRealVector point, final float maximalRadius) {
        final List<Element> result = new ArrayList<>();
        
        for( final var iterationElement : adjacentElements ) {
            final var distance = iterationElement.position.getDistance(point);
            if( distance < maximalRadius ) result.add(iterationElement);
        }
        
        return result;
    }
    
    private List<Element> getElementsInAdjacentCells(final Vector2d<Integer> center, final Vector2d<Integer> width) {
        final var minX = Math.max(0, center.x - width.x);
        final var maxX = Math.min(gridcountX - 1, center.x + width.x);
        final var minY = Math.max(0, center.y - width.y);
        final var maxY = Math.min(gridcountY-1, center.y+width.y);

        final List<Element> resultList = new ArrayList<>();
        
        for(var y = minY; y <= maxY; y++ )
            for (var x = minX; x <= maxX; x++) resultList.addAll(getCellAt(x, y).content);
        
        return resultList;
    }
    
    private Cell getCellAt(final int x, final int y) {
        return cells[x + y*gridcountX];
    }

    private Cell[] createCells(final int gridcountX, final int gridcountY) {

        final var result = (Cell[]) Array.newInstance(Cell.class, gridcountX * gridcountY);
        
        for(int i = 0; i < gridcountX*gridcountY; i++ )
            result[i] = new Cell();
        
        return result;
    }

    public Iterable<Element> getContentOfAllCells() {

        final Collection<Element> result = new ArrayList<>();
        
        for( final var iterationCell : cells ) result.addAll(iterationCell.content);
        
        return result;
    }

    
    //would help to make this static class
    public class Element {
        public ArrayRealVector position;
        public Type data;
    }

    //would help to make this static class
    private class Cell {
        public final Collection<Element> content = new ArrayList<>();
    }
    
    private final Cell[] cells;

    private final int gridcountX;
    private final int gridcountY;
    private final float sizeX;
    private final float sizeY;
    private final float gridelementSizeX;
    private final float gridelementSizeY;
}
