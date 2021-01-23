package basicplayer;

import java.util.Comparator;

import battlecode.common.MapLocation;

/**
 * Comparator class to compare two locations
 * Used for simple pathfinding and AI
 */
public class LocationComparator implements Comparator<Location> {

    /**
     * Compares two locations
     *
     * @return integer value. <0:l1<l2, =0:l1=l2, >0:l1>l2
     */
    public int compare( Location l1, Location l2 ) {
        return ( l1.compareTo( l2 ) );
    }
}
