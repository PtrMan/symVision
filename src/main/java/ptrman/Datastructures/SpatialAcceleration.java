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
    public SpatialAcceleration(int gridcountX, int gridcountY, float sizeX, float sizeY) {
        this.gridcountX = gridcountX;
        this.gridcountY = gridcountY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.gridelementSizeX = sizeX/gridcountX;
        this.gridelementSizeY = sizeY/gridcountY;
        
        
        this.cells = createCells(gridcountX, gridcountY);
    }
    
    public void flushCells() {
        for( Cell iterationCell : cells ) {
            iterationCell.content.clear();
        }
    }
    
    public void addElement(Element element) {

        Vector2d<Integer> center = calcCenterInt(element.position);
        getCellAt(center.x, center.y).content.add(element);
    }
    
    public List<Element> getElementsNearPoint(ArrayRealVector point, float maximalRadius) {

        Vector2d<Integer> neightborRadiusInBlocks = calcNeightborRadiusInBlocks(maximalRadius);
        Vector2d<Integer> center = calcCenterInt(point);

        List<Element> adjacentElements = getElementsInAdjacentCells(center, neightborRadiusInBlocks);
        List<Element> adjacentElementsInRadius = getElementsInRadius(adjacentElements, point, maximalRadius);
        return adjacentElementsInRadius;
    }
    
    private Vector2d<Integer> calcNeightborRadiusInBlocks(float maximalRadius) {
        Vector2d<Integer> neightborRadiusInBlocks;

        int neightborRadiusInBlocksX = 1 + (int) (maximalRadius / gridelementSizeX);
        int neightborRadiusInBlocksY = 1 + (int) (maximalRadius / gridelementSizeY);
        
        return new Vector2d<>(neightborRadiusInBlocksX, neightborRadiusInBlocksY);
    }
    
    private Vector2d<Integer> calcCenterInt(ArrayRealVector point) {

        int centerX = (int) (point.getDataRef()[0] / gridelementSizeX);
        int centerY = (int) (point.getDataRef()[1] / gridelementSizeY);

        return new Vector2d<>(centerX, centerY);
    }
    
    private List<Element> getElementsInRadius(Iterable<Element> adjacentElements, ArrayRealVector point, float maximalRadius) {
        List<Element> result = new ArrayList<>();
        
        for( Element iterationElement : adjacentElements ) {
            double distance = iterationElement.position.getDistance(point);
            if( distance < maximalRadius ) {
                result.add(iterationElement);
            }
        }
        
        return result;
    }
    
    private List<Element> getElementsInAdjacentCells(Vector2d<Integer> center, Vector2d<Integer> width) {
        final int minX = Math.max(0, center.x - width.x);
        final int maxX = Math.min(gridcountX - 1, center.x + width.x);
        final int minY = Math.max(0, center.y - width.y);
        final int maxY = Math.min(gridcountY-1, center.y+width.y);

        List<Element> resultList = new ArrayList<>();
        
        for( int y = minY; y <= maxY; y++ ) {
            for( int x = minX; x <= maxX; x++ ) {
                resultList.addAll(getCellAt(x, y).content);
            }
        }
        
        return resultList;
    }
    
    private Cell getCellAt(int x, int y) {
        return cells[x + y*gridcountX];
    }

    private Cell[] createCells(int gridcountX, int gridcountY) {
        int i;

        Cell[] result = (Cell[]) Array.newInstance(Cell.class, gridcountX * gridcountY);
        
        for( i = 0; i < gridcountX*gridcountY; i++ )
        {
            result[i] = new Cell();
        }
        
        return result;
    }

    public Iterable<Element> getContentOfAllCells() {

        Collection<Element> result = new ArrayList<>();
        
        for( Cell iterationCell : cells ) {
            result.addAll(iterationCell.content);
        }
        
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
