package cg4j.node;


import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

 class NodeTest {
    @Test
    void shapeEndCompatible1() {
        int[] shape1 = {5, 3, 4, 1, 4, 3, 2};
        int[] shape2 = {4, 1, 4, 3, 2};
        assertTrue(Node.ShapeEndCompatible(shape1, 0, shape2, 0));
    }

    @Test
    void shapeEndCompatible2() {
        int[] shape1 = {5, 3, 4, 1, 4, 3, 2};
        int[] shape2 = {3, 4, 1, 4, 3};
        assertTrue(Node.ShapeEndCompatible(shape1, 0, shape2, 1));
    }

    @Test
    void shapeEndCompatible3() {
        int[] shape1 = {4, 1, 4, 3, 2};
        int[] shape2 = {5, 3, 4, 1, 4, 3, 2};
        assertTrue(Node.ShapeEndCompatible(shape1, 0, shape2, 0));
    }

    @Test
    void shapeEndCompatible4() {
        int[] shape2 = {5, 3, 4, 1, 4, 3, 2};
        int[] shape1 = {3, 4, 1, 4, 3};
        assertTrue(Node.ShapeEndCompatible(shape1, 1, shape2, 0));
    }

    @Test
    void shapeEndCompatible5() {
        int[] shape1 = {5, 3, 4, 1, 4, 3, 2};
        int[] shape2 = {4, 2, 4, 3, 2};
        assertFalse(Node.ShapeEndCompatible(shape1, 0, shape2, 0));
    }

    @Test
    void shapeEndCompatible6() {
        int[] shape1 = {5, 3, 4, 1, 4, 3, 2};
        int[] shape2 = {4, 4, 1, 4, 3};
        assertFalse(Node.ShapeEndCompatible(shape1, 0, shape2, 1));
    }

    @Test
    void shapeEndCompatible7() {
        int[] shape1 = {4, 1, 4, 3, 2};
        int[] shape2 = {5, 3, 4, 1, 4, 3, 2};
        assertFalse(Node.ShapeEndCompatible(shape1, 0, shape2, 1));
    }

    @Test
    void shapeEndCompatible8() {
        int[] shape2 = {5, 3, 5, 1, 4, 3, 2};
        int[] shape1 = {3, 4, 1, 4, 3};
        assertFalse(Node.ShapeEndCompatible(shape1, 1, shape2, 0));
    }

}