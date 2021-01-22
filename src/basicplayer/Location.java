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
}
