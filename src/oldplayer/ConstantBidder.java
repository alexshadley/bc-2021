package oldplayer;

public class ConstantBidder implements Bidder {
    private final int bid;

    public ConstantBidder(final int bid) {
        this.bid = bid;
    }

    public int getBid(final boolean lastBidWon) {
        return bid;
    }
}
