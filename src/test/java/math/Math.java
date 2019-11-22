package math;

import org.junit.Test;

import static ptrman.math.Maths.modNegativeWraparound;

/**
 *
 */
public class Math {
    @Test
    public void testModNegativeWraparound() {
        assert modNegativeWraparound(0, 3) == 0 : "ASSERT: " + "";
        assert modNegativeWraparound(1, 3) == 1 : "ASSERT: " + "";
        assert modNegativeWraparound(2, 3) == 2 : "ASSERT: " + "";
        assert modNegativeWraparound(3, 3) == 0 : "ASSERT: " + "";

        assert modNegativeWraparound(-1, 3) == 2 : "ASSERT: " + "";
        assert modNegativeWraparound(-2, 3) == 1 : "ASSERT: " + "";
        assert modNegativeWraparound(-3, 3) == 0 : "ASSERT: " + "";
        assert modNegativeWraparound(-4, 3) == 2 : "ASSERT: " + "";
    }
}
