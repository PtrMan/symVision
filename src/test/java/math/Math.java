package math;

import org.junit.Test;
import ptrman.misc.Assert;

import static ptrman.math.Math.modNegativeWraparound;

/**
 *
 */
public class Math {
    @Test
    public void testModNegativeWraparound() {
        Assert.Assert(modNegativeWraparound(0, 3) == 0, "");
        Assert.Assert(modNegativeWraparound(1, 3) == 1, "");
        Assert.Assert(modNegativeWraparound(2, 3) == 2, "");
        Assert.Assert(modNegativeWraparound(3, 3) == 0, "");

        Assert.Assert(modNegativeWraparound(-1, 3) == 2, "");
        Assert.Assert(modNegativeWraparound(-2, 3) == 1, "");
        Assert.Assert(modNegativeWraparound(-3, 3) == 0, "");
        Assert.Assert(modNegativeWraparound(-4, 3) == 2, "");
    }
}
