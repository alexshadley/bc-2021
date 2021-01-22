package basicplayer;

import battlecode.common.MapLocation;

public class DestLocation extends Location {
    // Distance weight
    private double distance;

    public DestLocation( MapLocation location, double passability, double distance ) {
        super( location, passability );
        this.distance = distance;
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