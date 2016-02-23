package ptrman.nearestNeightbor;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;

public class NearestNeightbor {
    static public class Category {
        public ArrayRealVector position;
    }

    public List<Category> categories = new ArrayList<>();

    public int calcIndexOfNearest(final ArrayRealVector position) {
        int nearestIndex = 0;
        ArrayRealVector diff = position.subtract(categories.get(0).position);
        double nearestDistance = diff.dotProduct(diff);

        for( int i = 1; i < categories.size(); i++ ) {
            diff = position.subtract(categories.get(0).position);
            double currentDistance = diff.dotProduct(diff);

            if( currentDistance < nearestDistance ) {
                nearestIndex = i;
                nearestDistance = currentDistance;
            }
        }

        return nearestIndex;
    }
}
