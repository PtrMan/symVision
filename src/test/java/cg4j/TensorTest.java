package cg4j;



import cg4j.exception.IllegalShapeException;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

class TensorTest {

    @Test
    void constructorFail() {
        boolean a = true;
        try {
            Tensor tensor = new Tensor(new float[4 * 3 * 5], new int[]{4, 3, 5});
            assertTrue(true);
            a = false;
            tensor = new Tensor(new float[4 * 3 * 2], new int[]{4, 3, 5});
            fail();
        } catch (IllegalShapeException e) {
            assertFalse(a);
        }
    }

    @Test
    void getSetValI() {
        int[] shape = {4, 3, 2};
        float[] vals = new float[4 * 3 * 2];

        vals[2] = 1;
        vals[16] = 1;
        vals[23] = 1;

        Tensor tensor = new Tensor(vals, shape);

        for (int i = 0; i < 4 * 3 * 2; i++) {
            assertEquals(vals[i], tensor.get(i));
            float random = (float) Math.random();
            tensor.set(i, random);
            assertEquals(random, tensor.get(i));
        }
    }

    @Test
    void getSetValIs() {
        int[] shape = {4, 3, 2};
        float[] vals = new float[4 * 3 * 2];

        vals[2] = 1;
        vals[16] = 1;
        vals[23] = 1;

        Tensor tensor = new Tensor(vals, shape);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 2; z++) {
                    assertEquals(vals[x * 3 * 2 + y * 2 + z], tensor.get(new int[]{x, y, z}));
                    float random = (float) Math.random();
                    tensor.set(new int[]{x, y, z}, random);
                    assertEquals(random, tensor.get(new int[]{x, y, z}));
                }
            }
        }
    }

    @Test
    void indicesToAndFromIndex() {
        Tensor tensor = Tensor.fromRandom(-1, 1, new int[]{4, 3, 2});

        for (int i = 0; i < 4 * 3 * 2; i++) {
            assertEquals(i, tensor.getIndexFromIndices(tensor.getIndicesFromIndex(i)));
            assertEquals(tensor.get(i), tensor.get(tensor.getIndicesFromIndex(i)));
        }
    }

    @Test
    void fromRandomObject() {
        Random r = new Random(0);
        Tensor t = Tensor.fromRandom(r, -5, 3, new int[]{4, 6, 2});

        r.setSeed(0);
        Tensor t2 = Tensor.fromRandom(r, -5, 3, new int[]{4, 6, 2});
        Tensor t3 = Tensor.fromRandom(r, -5, 3, new int[]{4, 6, 2});
        assertEquals(t, t2);
        assertNotEquals(t, t3);

        assertEquals(4 * 6 * 2, t.length);
        assertArrayEquals(new int[]{4, 6, 2}, t.shape);

        float lowest = 100;
        float highest = -100;
        for (int i = 0; i < t.length; i++) {
            lowest = Math.min(lowest, t.get(i));
            highest = Math.max(highest, t.get(i));
        }
        assertEquals(Math.round(lowest), -5);
        assertEquals(3, Math.round(highest));
    }

    @Test
    void fromRandomMath() {
        Tensor t = Tensor.fromRandom(10, 49, new int[]{6, 6, 2, 10, 10});

        assertEquals(6 * 6 * 2 * 10 * 10, t.length);
        assertArrayEquals(new int[]{6, 6, 2, 10, 10}, t.shape);

        float lowest = 100;
        float highest = -100;
        for (int i = 0; i < t.length; i++) {
            lowest = Math.min(lowest, t.get(i));
            highest = Math.max(highest, t.get(i));
        }
        assertEquals(10, Math.round(lowest));
        assertEquals(49, Math.round(highest));
    }

    @Test
    void toStringTest() {
        Tensor tensor = new Tensor(new float[]{
                0, 1,
                2, 3,
                4, 5,

                6, 7,
                8, 9,
                10, 11
        }, new int[]{2, 3, 2});

        assertEquals("Tensor(Shape=[2, 3, 2], Data=[[[0.0, 1.0], [2.0, 3.0], [4.0, 5.0]], [[6.0, 7.0], [8.0, 9.0], [10.0, 11.0]]])", tensor.toString());
    }

    @Test
    void arrayToStringTest() {
        Tensor tensor = new Tensor(new float[]{
                0, 1,
                2, 3,
                4, 5,

                6, 7,
                8, 9,
                10, 11
        }, new int[]{2, 3, 2});

        assertEquals("[[[0.0, 1.0], [2.0, 3.0], [4.0, 5.0]], [[6.0, 7.0], [8.0, 9.0], [10.0, 11.0]]]", tensor.arrayToString());
    }

}