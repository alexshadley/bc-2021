package basicplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class BidderRunner {
    private int previousVotes;

    public BidderRunner(final int currentVotes) {
        this.previousVotes = currentVotes;
    }

    public void attemptBid(final RobotController rc, final Bidder bidder) {
        final boolean lastVoteWon = rc.getTeamVotes() > previousVotes;
        previousVotes = rc.getTeamVotes();

        final int bid = bidder.getBid(lastVoteWon);
        if (rc.canBid(bid)) {
            try {
                rc.bid(bid);
            } catch (final GameActionException e) {
                // should never happen
                e.printStackTrace();
            }
        }
    }
}
