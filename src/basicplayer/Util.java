package basicplayer;

import java.util.Map;
import java.util.HashMap;

public class Util {
    // Slanderer cost for influence return
    public static Map<Integer, Integer> slandererInfluenceCost;
    static {
        slandererInfluenceCost = new HashMap<>();
        slandererInfluenceCost.put( 1, 21 );
        slandererInfluenceCost.put( 2, 41 );
        slandererInfluenceCost.put( 3, 63 );
        slandererInfluenceCost.put( 4, 85 );
        slandererInfluenceCost.put( 5, 107 );
        slandererInfluenceCost.put( 6, 130 );
        slandererInfluenceCost.put( 7, 154 );
        slandererInfluenceCost.put( 8, 178 );
        slandererInfluenceCost.put( 9, 203 );
        slandererInfluenceCost.put( 10, 228 );
        slandererInfluenceCost.put( 11, 255 );
        slandererInfluenceCost.put( 12, 282 );
        slandererInfluenceCost.put( 13, 310 );
        slandererInfluenceCost.put( 14, 339 );
        slandererInfluenceCost.put( 15, 368 );
    }

    /**
     * Get cost of slanderer to produce desired influence return rate
     *
     * @param desiredReturnRate Influence return rate for slanderer
     * @return Influence needed to create slanderer
     */
    public static int getSlandererInfluenceCost( int desiredReturnRate ) {
        int influenceCost = -1;

        if ( slandererInfluenceCost.containsKey( desiredReturnRate ) ) {
            influenceCost = slandererInfluenceCost.get( desiredReturnRate );
        }

        return ( influenceCost );
    }
}
