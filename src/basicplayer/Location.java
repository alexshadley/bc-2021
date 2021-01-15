package basicplayer;

import battlecode.common.MapLocation;

/**
 * Location class to store information about a MapLocation
 */
public class Location {
    // Map Location
    private MapLocation location;

    // Passability
    private double passability;

    // Distance weight
    private double distance;

    // TODO: add corner weight

    /**
     * Constructor to initialize a location
     */
    public Location( MapLocation location, double passability, double distance ) {
        this.location = location;
        this.passability = passability;
        this.distance = distance;
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
     * Get distance to destination from this location
     *
     * @return distance to destination from location
     */
    public double getDistance() {
        return ( distance );
    }
}
