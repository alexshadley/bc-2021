package common;

public class LinearBidder implements Bidder {
    private static int MAX_NUM_TIMES_USED_MIN = 10;

    private final int startingBid;
    private final int step;
    private final int maxBid;

    private int previousBid;
    private int minSuccessful = 0;
    private int numTimesUsedMin = 0;

    public LinearBidder(final int startingBid, final int step, final int maxBid) {
        this.startingBid = startingBid;
        this.step = step;
        this.maxBid = maxBid;

        this.previousBid = startingBid;
    }

    // pass-through to record previous bid
    public int getBid(final boolean wonLastBid) {
        final int bid = Math.min(getBidInternal(wonLastBid), maxBid);
        previousBid = bid;
        return bid;
    }

    public int getBidInternal(final boolean wonLastBid) {
        if (wonLastBid) {
            minSuccessful = previousBid;
        }

        if (minSuccessful != 0) {
            // go up if we're no longer winning with this
            if (!wonLastBid) {
                final int bid = minSuccessful + step;
                minSuccessful = 0;
                return bid;
            }
            // go down if we've been winning too long and could get cheaper wins
            if (numTimesUsedMin <= MAX_NUM_TIMES_USED_MIN) {
                final int bid = minSuccessful + step;
                minSuccessful = 0;
                return bid;
            } else {
                numTimesUsedMin++;
                return minSuccessful;
            }
        }

        return previousBid + step;
    }
}
