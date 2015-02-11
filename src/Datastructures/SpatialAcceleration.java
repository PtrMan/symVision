package Datastructures;

import static Datastructures.Vector2d.FloatHelper.getLength;
import static Datastructures.Vector2d.FloatHelper.sub;
import java.lang.reflect.Array;
import java.util.ArrayList;
import misc.Assert;

/**
 *
 * 
 */
public class SpatialAcceleration<Type>
{
    private final int gridcountX;
    private final int gridcountY;
    private final float sizeX;
    private final float sizeY;
    private final float gridelementSizeX;
    private final float gridelementSizeY;
    
    public SpatialAcceleration(int gridcountX, int gridcountY, float sizeX, float sizeY)
    {
        this.gridcountX = gridcountX;
        this.gridcountY = gridcountY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.gridelementSizeX = sizeX/gridcountX;
        this.gridelementSizeY = sizeY/gridcountY;
        
        
        this.cells = createCells(gridcountX, gridcountY);
    }
    
    public void flushCells()
    {
        for( Cell iterationCell : cells )
        {
            iterationCell.content.clear();
        }
    }
    
    public void addElement(Element element)
    {
        Vector2d<Integer> center;
        
        center = calcCenterInt(element.position);
        getCellAt(center.x, center.y).content.add(element);
    }
    
    public ArrayList<Element> getElementsNearPoint(Vector2d<Float> point, float maximalRadius)
    {
        Vector2d<Integer> neightborRadiusInBlocks;
        Vector2d<Integer> center;
        ArrayList<Element> adjacentElements;
        ArrayList<Element> adjacentElementsInRadius;
        
        Assert.Assert(point.x >= 0.0f, "");
        Assert.Assert(point.y >= 0.0f, "");
        
        Assert.Assert(point.x <= sizeX, "");
        Assert.Assert(point.y <= sizeY, "");
        
        neightborRadiusInBlocks = calcNeightborRadiusInBlocks(maximalRadius);
        center = calcCenterInt(point);
        
        adjacentElements = getElementsInAdjacentCells(center, neightborRadiusInBlocks);
        adjacentElementsInRadius = getElementsInRadius(adjacentElements, point, maximalRadius);
        return adjacentElementsInRadius;
    }
    
    private Vector2d<Integer> calcNeightborRadiusInBlocks(float maximalRadius)
    {
        Vector2d<Integer> neightborRadiusInBlocks;
        int neightborRadiusInBlocksX, neightborRadiusInBlocksY;
        
        neightborRadiusInBlocksX = 1 + (int)(maximalRadius / gridelementSizeX);
        neightborRadiusInBlocksY = 1 + (int)(maximalRadius / gridelementSizeY);
        
        return new Vector2d<Integer>(neightborRadiusInBlocksX, neightborRadiusInBlocksY);
    }
    
    private Vector2d<Integer> calcCenterInt(Vector2d<Float> point)
    {
        int centerX, centerY;
        
        centerX = (int)(point.x / gridelementSizeX);
        centerY = (int)(point.y / gridelementSizeY);
        
        return new Vector2d<Integer>(centerX, centerY);
    }
    
    private ArrayList<Element> getElementsInRadius(ArrayList<Element> adjacentElements, Vector2d<Float> point, float maximalRadius)
    {
        ArrayList<Element> result;
        
        result = new ArrayList<Element>();
        
        for( Element iterationElement : adjacentElements )
        {
            Vector2d<Float> diff;
            float distance;
            
            diff = sub(iterationElement.position, point);
            distance = getLength(diff);
            if( distance < maximalRadius )
            {
                result.add(iterationElement);
            }
        }
        
        return result;
    }
    
    private ArrayList<Element> getElementsInAdjacentCells(Vector2d<Integer> center, Vector2d<Integer> width)
    {
        int minX, minY, maxX, maxY;
        int x, y;
        ArrayList<Element> resultList;
        
        minX = Math.max(0, center.x-width.x);
        maxX = Math.min(gridcountX-1, center.x+width.x);
        minY = Math.max(0, center.y-width.y);
        maxY = Math.min(gridcountY-1, center.y+width.y);
        
        resultList = new ArrayList<>();
        
        for( y = minY; y <= maxY; y++ )
        {
            for( x = minX; x <= maxX; x++ )
            {
                resultList.addAll(getCellAt(x, y).content);
            }
        }
        
        return resultList;
    }
    
    private Cell getCellAt(int x, int y)
    {
        return cells[x + y*gridcountX];
    }

    private Cell[] createCells(int gridcountX, int gridcountY)
    {
        Cell[] result;
        int i;
        
        result = (Cell[]) Array.newInstance(Cell.class, gridcountX*gridcountY);
        
        for( i = 0; i < gridcountX*gridcountY; i++ )
        {
            result[i] = new Cell();
        }
        
        return result;
    }

    public Iterable<Element> getContentOfAllCells()
    {
        ArrayList<Element> result;
        
        result = new ArrayList<Element>();
        
        for( Cell iterationCell : cells )
        {
            result.addAll(iterationCell.content);
        }
        
        return result;
    }

    
    
    public class Element
    {
        public Vector2d<Float> position;
        public Type data;
    }
    
    private class Cell
    {
        ArrayList<Element> content = new ArrayList<Element>();
    }
    
    private Cell cells[];
}
