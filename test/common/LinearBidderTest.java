package common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LinearBidderTest {
    @Test
    public void testLinearBidder() {
        final LinearBidder bidder = new LinearBidder(2, 2, 10);

        assertEquals(
            2,
            bidder.getBid(false)
        );
        assertEquals(
            4,
            bidder.getBid(false)
        );
        assertEquals(
            6,
            bidder.getBid(false)
        );
        assertEquals(
            6,
            bidder.getBid(true)
        );
        assertEquals(
            8,
            bidder.getBid(false)
        );

        for (int i = 0; i < LinearBidder.MAX_NUM_TIMES_USED_MIN; i++) {
            assertEquals(
                8,
                bidder.getBid(true)
            );
        }

        assertEquals(
            6,
            bidder.getBid(true)
        );
        assertEquals(
            6,
            bidder.getBid(true)
        );
    }
}
