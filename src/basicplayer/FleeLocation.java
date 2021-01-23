package basicplayer;

import java.util.List;

import battlecode.common.MapLocation;

/**
 * Flee Location for information on how to flee
 */
public class FleeLocation extends Location {
    // Lists of enemies and ECs
    private final List<MapLocation> enemies;
    private final List<MapLocation> enlightenmentCenters;

    /**
     * Constructor to initialize a location
     */
    public FleeLocation( MapLocation location, double passability, List<MapLocation> enemies, List<MapLocation> enlightenmentCenters ) {
        super( location, passability );

        this.enemies = enemies;
        this.enlightenmentCenters = enlightenmentCenters;
    }

    /**
     * Get list of enemies sensed by robot
     *
     * @return list of enemies
     */
    public List<MapLocation> getEnemies() {
        return ( enemies );
    }

    /**
     * Get list of ECs sensed by robot
     *
     * @return list of enlightenment centers
     */
    public List<MapLocation> getEnlightenmentCenters() {
        return ( enlightenmentCenters );
    }

    /**
     * Compare locations for fleeing from enemies
     *
     * @return integer value
     * < 0 : this location is less than l2
     * = 0 : locations are equal
     * > 0 : this location is greater than l2
     */
    @Override
    protected int compareTo( Location loc2 ) {
        // Cast to FleeLocation
        FleeLocation l2 = (FleeLocation) loc2;

        // Get average distance from enemies and ECs for each location
        double l1AvgEnemyDist = 0;
        double l2AvgEnemyDist = 0;

        int numEnemies = enemies.size();
        if ( 0 < numEnemies ) {
            for ( MapLocation loc : enemies ) {
                l1AvgEnemyDist += location.distanceSquaredTo( loc );
                l2AvgEnemyDist += l2.getLocation().distanceSquaredTo( loc );
            }

            l1AvgEnemyDist /= numEnemies;
            l2AvgEnemyDist /= numEnemies;

            l1AvgEnemyDist = 100 / l1AvgEnemyDist;
            l2AvgEnemyDist = 100 / l2AvgEnemyDist;
        }

        double l1AvgECDist = 0;
        double l2AvgECDist = 0;
        
        int numECs = enlightenmentCenters.size();
        if ( 0 < numECs ) {
            for ( MapLocation loc : enlightenmentCenters ) {
                l1AvgECDist += location.distanceSquaredTo( loc );
                l2AvgECDist += l2.getLocation().distanceSquaredTo( loc );
            }

            l1AvgECDist /= numECs;
            l2AvgECDist /= numECs;

            l1AvgECDist = 100 / l1AvgECDist;
            l2AvgECDist = 100 / l2AvgECDist;
        }

        // Final cost function
        double l1Cost = l1AvgEnemyDist + l1AvgECDist + 5.0 / passability;
        double l2Cost = l2AvgEnemyDist + l2AvgECDist + 5.0 / l2.getPassability();

        return ( (int) ( l1Cost - l2Cost ) );
    }
}
