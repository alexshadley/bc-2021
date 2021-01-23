package basicplayer;

import battlecode.common.MapLocation;

/**
 * Location class to store information about a MapLocation
 */
public class Location {
    // Map Location
    protected MapLocation location;

    // Passability
    protected double passability;

    /**
     * Constructor to initialize a location
     */
    public Location( MapLocation location, double passability ) {
        this.location = location;
        this.passability = passability;
    }

    /**
     * Get MapLocation object
     *
     * @return MapLocation
     */
    public MapLocation getLocation() {
        return ( location );
    }

    /**
     * Get passability value of location
     *
     * @return passability double [0.1, 1.0]
     */
    public double getPassability() {
        return ( passability );
    }

    /**
     * Compare locations
     * 
     * @return integer value
     * < 0 : this location is less than l2
     * = 0 : locations are equal
     * > 0 : this location is greater than l2
     */
    protected int compareTo( Location l2 ) {
        double l1Cost = 5.0 / passability;
        double l2Cost = 5.0 / l2.getPassability();

        return ( (int) ( l1Cost - l2Cost ) );
    }
}
