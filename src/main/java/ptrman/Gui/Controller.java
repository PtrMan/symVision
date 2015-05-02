package ptrman.Gui;

// TODO< split this into multiple classes >
public class Controller
{

        // function in here because we don't know who should have it
        /**

        
        private static void drawTestTriangle(Graphics2D graphics, Vector2d<Float> position, float radius, float angle, float relativeSegmentWidth)
        {
            drawTrianglePart(graphics, position, radius, angle                                      , radius * relativeSegmentWidth);
            drawTrianglePart(graphics, position, radius, angle + (2.0f*(float)Math.PI * (1.0f/3.0f)), radius * relativeSegmentWidth);
            //test drawTrianglePart(graphics, position, radius, angle + (2.0f*(float)Math.PI * (2.0f/3.0f)), radius * relativeSegmentWidth);
        }
        
        private static void drawTrianglePart(Graphics2D graphics, Vector2d<Float> center, float radius, float angle, float segmentWidth)
        {
            Vector2d<Float> tangent, scaledTangent;
            Vector2d<Float> normal, scaledNormal;
            Vector2d<Float> pointA, pointB;
            Vector2d<Integer> pointAInt, pointBInt;
            
            normal = new Vector2d<Float>((float)Math.sin(angle), (float)Math.cos(angle));
            tangent = new Vector2d<Float>(normal.y, -normal.x);
            
            scaledNormal = getScaled(normal, radius);
            scaledTangent = getScaled(tangent, segmentWidth);
            
            pointA = add(center, add(scaledNormal, scaledTangent));
            pointB = add(center, sub(scaledNormal, scaledTangent));
            
            pointAInt = new Vector2d<>(Math.round(pointA.x), Math.round(pointA.y));
            pointBInt = new Vector2d<>(Math.round(pointB.x), Math.round(pointB.y));
            
            graphics.drawLine(pointAInt.x, pointAInt.y, pointBInt.x, pointBInt.y);
        }*/

}
